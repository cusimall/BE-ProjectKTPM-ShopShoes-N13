package com.microservice.cartservice.services.impl;

import com.microservice.cartservice.client.ProductClient;
import com.microservice.cartservice.client.UserClient;
import com.microservice.cartservice.dto.InvoiceDTO;
import com.microservice.cartservice.dto.InvoiceItemDTO;
import com.microservice.cartservice.dto.ProductDTO;
import com.microservice.cartservice.dto.UserDTO;
import com.microservice.cartservice.models.Cart;
import com.microservice.cartservice.models.CartDetails;
import com.microservice.cartservice.models.Product;
import com.microservice.cartservice.models.User;
import com.microservice.cartservice.repository.CartDetailsRepository;
import com.microservice.cartservice.repository.CartRepository;
import com.microservice.cartservice.repository.ProductRepository;
import com.microservice.cartservice.repository.UserRepository;
import com.microservice.cartservice.services.CartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartServiceImpl implements CartService {
    private final CartRepository cartRepository;
    private final CartDetailsRepository cartDetailsRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final ProductClient productClient;
    private final UserClient userClient;
    private final WebClient.Builder webClientBuilder;
    
    @Override
    @Transactional
    public Cart getOrCreateCart(Long userId, String authToken) {
        Optional<Cart> existingCart = cartRepository.findByUserId(userId);
        
        if (existingCart.isPresent()) {
            return existingCart.get();
        }
        
        // Get user details from auth service
        UserDTO userDTO = userClient.getUserById(userId, authToken);
        
        // Create local user entity
        User user = userRepository.findById(userId)
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setId(userDTO.getId());
                    newUser.setUsername(userDTO.getUsername());
                    newUser.setEmail(userDTO.getEmail());
                    return userRepository.save(newUser);
                });
        
        // Create new cart
        Cart cart = new Cart();
        cart.setUserId(userId);
        cart.setUser(user);
        cart.setTotal(BigDecimal.ZERO);
        
        return cartRepository.save(cart);
    }
    
    @Override
    @Transactional
    public Cart addProductToCart(Long cartId, Long productId, int quantity, String authToken) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));
        
        // Check if product already exists in cart
        Optional<CartDetails> existingDetail = cart.getCartDetails().stream()
                .filter(detail -> detail.getProductId().equals(productId))
                .findFirst();
        
        if (existingDetail.isPresent()) {
            // Update existing cart item
            CartDetails detail = existingDetail.get();
            detail.setQuantity(detail.getQuantity() + quantity);
            detail.setTotal(detail.getProduct().getProductPrice().multiply(BigDecimal.valueOf(detail.getQuantity())));
            cartDetailsRepository.save(detail);
        } else {
            // Fetch product from product service
            ProductDTO productDTO = productClient.getProductById(productId, authToken);
            
            // Create or get local product entity
            Product product = productRepository.findById(productId)
                    .orElseGet(() -> {
                        Product newProduct = new Product();
                        newProduct.setId(productDTO.getId());
                        newProduct.setProductName(productDTO.getProductName());
                        newProduct.setProductDescription(productDTO.getProductDescription());
                        newProduct.setProductPrice(productDTO.getProductPrice());
                        newProduct.setImageUrl(productDTO.getImageUrl());
                        return productRepository.save(newProduct);
                    });
            
            // Create new cart detail
            CartDetails cartDetail = new CartDetails();
            cartDetail.setCart(cart);
            cartDetail.setProductId(productId);
            cartDetail.setProduct(product);
            cartDetail.setQuantity(quantity);
            cartDetail.setTotal(product.getProductPrice().multiply(BigDecimal.valueOf(quantity)));
            
            cart.getCartDetails().add(cartDetail);
            cartDetailsRepository.save(cartDetail);
        }
        
        // Update cart total
        cart.updateTotalPrice();
        return cartRepository.save(cart);
    }
    
    @Override
    public List<CartDetails> getAllCartDetails(Long cartId) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));
        return cart.getCartDetails();
    }
    
    @Override
    @Transactional
    public void removeProductFromCart(Long cartId, Long productId) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));
        
        cart.getCartDetails().removeIf(detail -> detail.getProductId().equals(productId));
        cart.updateTotalPrice();
        cartRepository.save(cart);
    }
    
    @Override
    @Transactional
    public void increaseCartItemQuantity(Long cartId, Long productId) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));
        
        cart.getCartDetails().stream()
                .filter(detail -> detail.getProductId().equals(productId))
                .findFirst()
                .ifPresent(detail -> {
                    detail.setQuantity(detail.getQuantity() + 1);
                    detail.setTotal(detail.getProduct().getProductPrice().multiply(BigDecimal.valueOf(detail.getQuantity())));
                    cartDetailsRepository.save(detail);
                    cart.updateTotalPrice();
                    cartRepository.save(cart);
                });
    }
    
    @Override
    @Transactional
    public void decreaseCartItemQuantity(Long cartId, Long productId) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));
        
        cart.getCartDetails().stream()
                .filter(detail -> detail.getProductId().equals(productId))
                .findFirst()
                .ifPresent(detail -> {
                    if (detail.getQuantity() > 1) {
                        detail.setQuantity(detail.getQuantity() - 1);
                        detail.setTotal(detail.getProduct().getProductPrice().multiply(BigDecimal.valueOf(detail.getQuantity())));
                        cartDetailsRepository.save(detail);
                    } else {
                        cart.getCartDetails().remove(detail);
                        cartDetailsRepository.delete(detail);
                    }
                    cart.updateTotalPrice();
                    cartRepository.save(cart);
                });
    }
    
    @Override
    @Transactional
    public void createInvoiceFromCart(Long cartId, String authToken) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));
        
        if (cart.getCartDetails().isEmpty()) {
            throw new RuntimeException("Cannot create invoice from empty cart");
        }
        
        // Create invoice DTO to send to invoice service
        InvoiceDTO invoiceDTO = new InvoiceDTO();
        invoiceDTO.setUserId(cart.getUserId());
        invoiceDTO.setTotalAmount(cart.getTotal());
        invoiceDTO.setCreatedDate(LocalDateTime.now());
        
        // Convert cart details to invoice items
        List<InvoiceItemDTO> items = cart.getCartDetails().stream()
                .map(detail -> {
                    InvoiceItemDTO item = new InvoiceItemDTO();
                    item.setProductId(detail.getProductId());
                    item.setProductName(detail.getProduct().getProductName());
                    item.setQuantity(detail.getQuantity());
                    item.setPrice(detail.getProduct().getProductPrice());
                    item.setTotal(detail.getTotal());
                    return item;
                })
                .collect(Collectors.toList());
        
        invoiceDTO.setItems(items);
        
        // Call invoice service through API gateway
        try {
            webClientBuilder.build()
                    .post()
                    .uri("http://localhost:8081/api/invoices/create")
                    .header("Authorization", "Bearer " + authToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(invoiceDTO)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();
            
            log.info("Invoice created successfully for cart ID: {}", cartId);
            
            // Clear cart after successful invoice creation
            cart.getCartDetails().clear();
            cart.setTotal(BigDecimal.ZERO);
            cartRepository.save(cart);
        } catch (Exception e) {
            log.error("Error creating invoice: {}", e.getMessage());
            throw new RuntimeException("Failed to create invoice: " + e.getMessage());
        }
    }
}
