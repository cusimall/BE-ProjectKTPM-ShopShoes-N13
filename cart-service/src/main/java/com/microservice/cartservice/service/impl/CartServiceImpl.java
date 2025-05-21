package com.microservice.cartservice.service.impl;

import com.microservice.cartservice.dto.CartRequest;
import com.microservice.cartservice.dto.ProductDTO;
import com.microservice.cartservice.models.Cart;
import com.microservice.cartservice.models.CartDetails;
import com.microservice.cartservice.repository.CartDetailsRepository;
import com.microservice.cartservice.repository.CartRepository;
import com.microservice.cartservice.service.CartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.DeserializationFeature;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartDetailsRepository cartDetailsRepository;
    private final WebClient.Builder webClientBuilder;
    
    @Value("${api.gateway.url}")
    private String apiGatewayUrl;

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public Cart getOrCreateCart(Long userId) {
        return cartRepository.findByUserId(userId)
                .orElseGet(() -> {
                    Cart cart = new Cart();
                    cart.setUserId(userId);
                    cart.setTotal(BigDecimal.ZERO);
                    return cartRepository.save(cart);
                });
    }

    @Override
    @Transactional
    public Cart addToCart(Long userId, CartRequest request, String token) {
        Cart cart = getOrCreateCart(userId);
        
        // Get product information from product service
        ProductDTO product = getProductDetails(request.getProductId(), token);
        
        // Check if item already exists in cart
        Optional<CartDetails> existingDetail = cartDetailsRepository.findByCartCartIdAndProductId(
                cart.getCartId(), request.getProductId());
        
        CartDetails cartDetail;
        if (existingDetail.isPresent()) {
            // Update existing item
            cartDetail = existingDetail.get();
            cartDetail.setQuantity(cartDetail.getQuantity() + request.getQuantity());
            cartDetail.setTotal(product.getProductPrice().multiply(
                    BigDecimal.valueOf(cartDetail.getQuantity())));
        } else {
            // Create new cart detail
            cartDetail = new CartDetails();
            cartDetail.setCart(cart);
            cartDetail.setProductId(product.getId());
            cartDetail.setQuantity(request.getQuantity());
            cartDetail.setTotal(product.getProductPrice().multiply(
                    BigDecimal.valueOf(request.getQuantity())));
            cart.getCartDetails().add(cartDetail);
        }
        
        cartDetailsRepository.save(cartDetail);
        
        // Recalculate cart total
        cart.calculateTotal();
        return cartRepository.save(cart);
    }

    @Override
    public List<CartDetails> getCartDetails(Long cartId) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));
                
        // Load product details for each cart item
        cart.getCartDetails().forEach(detail -> {
            try {
                ProductDTO product = getProductDetails(detail.getProductId(), null);
                detail.setProduct(product);
            } catch (Exception e) {
                log.error("Error loading product {}: {}", detail.getProductId(), e.getMessage());
                // Set null instead of throwing exception
                detail.setProduct(null);
            }
        });
        
        return cart.getCartDetails();
    }

    @Override
    @Transactional
    public void removeFromCart(Long cartId, Long productId) {
        try {
            // 1. Tìm cart
            Cart cart = cartRepository.findById(cartId)
                    .orElseThrow(() -> new RuntimeException("Cart not found"));
                    
            // 2. Xóa relationship trong bộ nhớ
            cart.getCartDetails().removeIf(detail -> detail.getProductId().equals(productId));
            
            // 3. Lưu cart để cập nhật lại relationship
            cartRepository.save(cart);
            
            // 4. Sau đó xóa cart detail từ repository
            cartDetailsRepository.deleteByCartCartIdAndProductId(cartId, productId);
            
            // 5. Tính toán lại tổng giỏ hàng
            BigDecimal total = BigDecimal.ZERO;
            for (CartDetails detail : cart.getCartDetails()) {
                if (detail.getTotal() != null) {
                    total = total.add(detail.getTotal());
                }
            }
            cart.setTotal(total);
            
            // 6. Lưu lại cart
            cartRepository.save(cart);
            
            // 7. Clear Hibernate session để đảm bảo
            entityManager.flush();
            entityManager.clear();
            
        } catch (Exception e) {
            log.error("Error removing item from cart: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to remove item from cart: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public CartDetails updateQuantity(Long cartId, Long productId, int quantity, String token) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));
        
        CartDetails cartDetail = cartDetailsRepository.findByCartCartIdAndProductId(cartId, productId)
                .orElseThrow(() -> new RuntimeException("Product not found in cart"));
        
        if (quantity <= 0) {
            // Remove item
            removeFromCart(cartId, productId);
            return null;
        } else {
            // Update quantity
            ProductDTO product = getProductDetails(productId, token);
            cartDetail.setQuantity(quantity);
            cartDetail.setTotal(product.getProductPrice().multiply(BigDecimal.valueOf(quantity)));
            cartDetailsRepository.save(cartDetail);
            
            // Update cart total
            cart.calculateTotal();
            cartRepository.save(cart);
            
            return cartDetail;
        }
    }

    @Override
    @Transactional
    public void checkout(Long cartId, String token) {
        try {
            Cart cart = cartRepository.findById(cartId)
                    .orElseThrow(() -> new RuntimeException("Cart not found"));
            
            if (cart.getCartDetails().isEmpty()) {
                throw new RuntimeException("Cannot checkout empty cart");
            }
            
            // Process token
            String actualToken = token;
            if (token.startsWith("Bearer ")) {
                actualToken = token.substring(7);
            }

            log.info("Token format check - First 10 chars: {}",
                    actualToken.length() > 10 ? actualToken.substring(0, 10) + "..." : actualToken);

            // Create request body
            Map<String, Object> invoiceRequest = new HashMap<>();
            invoiceRequest.put("cartId", cartId);
            invoiceRequest.put("userId", cart.getUserId());
            
            // Fix the URI path - ADD "v1/" to the path
            Object response = webClientBuilder.build()
                    .post()
                    .uri(apiGatewayUrl + "/api/v1/invoices/create-from-cart") // CORRECTED PATH WITH v1/
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + actualToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(invoiceRequest)
                    .retrieve()
                    .bodyToMono(Object.class)
                    .block();
            
            log.info("Invoice created successfully: {}", response);
            
            // Clear cart after successful checkout
            clearCart(cartId);
            
            log.info("Checkout completed successfully for cart ID: {}", cartId);
        } catch (Exception e) {
            log.error("Error during checkout: {}", e.getMessage(), e);
            throw new RuntimeException("Checkout failed: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public void clearCart(Long cartId) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));
        
        cart.getCartDetails().clear();
        cart.setTotal(BigDecimal.ZERO);
        cartRepository.save(cart);
    }
    
    private ProductDTO getProductDetails(Long productId, String token) {
        log.info("Fetching product details for ID: {}", productId);
        
        try {
            WebClient.RequestHeadersSpec<?> request = webClientBuilder.build()
                    .get()
                    .uri(apiGatewayUrl + "/api/products/internal/" + productId);
                    
            if (token != null && !token.isEmpty()) {
                request = request.header(HttpHeaders.AUTHORIZATION, "Bearer " + token);
            }
            
            // Gọi API và lấy dữ liệu trực tiếp
            ProductDTO product = request.retrieve()
                    .bodyToMono(ProductDTO.class)
                    .block();
                    
            log.info("Successfully retrieved product: {}", product);
            return product;
        } catch (Exception e) {
            log.error("Error fetching product details: {}", e.getMessage(), e);
            return null; // Trả về null thay vì throw exception
        }
    }
} 