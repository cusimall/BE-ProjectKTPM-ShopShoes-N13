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
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
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
            }
        });
        
        return cart.getCartDetails();
    }

    @Override
    @Transactional
    public void removeFromCart(Long cartId, Long productId) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));
        
        // Remove item
        cartDetailsRepository.deleteByCartCartIdAndProductId(cartId, productId);
        
        // Reload cart and update total
        cart = cartRepository.findById(cartId).get();
        cart.calculateTotal();
        cartRepository.save(cart);
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
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));
        
        if (cart.getCartDetails().isEmpty()) {
            throw new RuntimeException("Cannot checkout empty cart");
        }
        
        // Call invoice service to create invoice
        Map<String, Object> invoiceRequest = new HashMap<>();
        invoiceRequest.put("cartId", cartId);
        invoiceRequest.put("userId", cart.getUserId());
        
        try {
            webClientBuilder.build()
                    .post()
                    .uri(apiGatewayUrl + "/api/invoices/create-from-cart")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(invoiceRequest)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();
            
            // Clear cart after successful checkout
            clearCart(cartId);
            
            log.info("Checkout completed successfully for cart ID: {}", cartId);
        } catch (Exception e) {
            log.error("Error during checkout: {}", e.getMessage());
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
        WebClient.RequestHeadersSpec<?> request = webClientBuilder.build()
                .get()
                .uri(apiGatewayUrl + "/api/products/" + productId);
                
        if (token != null && !token.isEmpty()) {
            request = request.header(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        }
        
        try {
            return request.retrieve()
                    .bodyToMono(ProductDTO.class)
                    .block();
        } catch (Exception e) {
            log.error("Error fetching product details: {}", e.getMessage());
            throw new RuntimeException("Failed to get product details: " + e.getMessage());
        }
    }
} 