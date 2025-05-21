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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;

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
        
        // If no token provided, try to get it from SecurityContext
        String actualToken = token;
        if (actualToken == null || actualToken.isEmpty()) {
            actualToken = extractTokenFromSecurityContext();
            log.debug("Using token from security context: {}", actualToken != null ? "Available" : "Not available");
        }
        
        // Get product information from product service
        ProductDTO product = getProductDetails(request.getProductId(), actualToken);
        
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

    /**
     * Helper method to extract JWT token from SecurityContext
     */
    private String extractTokenFromSecurityContext() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getCredentials() instanceof String) {
            return (String) authentication.getCredentials();
        }
        return null;
    }

    @Override
    public List<CartDetails> getCartDetails(Long cartId) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));
        
        // Get current authentication token if available
        String token = extractTokenFromSecurityContext();
        log.debug("Using token for product details: {}", token != null ? "Available" : "Not available");
                
        // Load product details for each cart item
        cart.getCartDetails().forEach(detail -> {
            try {
                ProductDTO product = getProductDetails(detail.getProductId(), token);
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
            // 1. Find cart
            Cart cart = cartRepository.findById(cartId)
                    .orElseThrow(() -> new RuntimeException("Cart not found"));
                    
            // 2. Remove relationship in memory
            cart.getCartDetails().removeIf(detail -> detail.getProductId().equals(productId));
            
            // 3. Save cart to update relationship
            cartRepository.save(cart);
            
            // 4. Delete cart detail from repository
            cartDetailsRepository.deleteByCartCartIdAndProductId(cartId, productId);
            
            // 5. Recalculate cart total
            BigDecimal total = BigDecimal.ZERO;
            for (CartDetails detail : cart.getCartDetails()) {
                if (detail.getTotal() != null) {
                    total = total.add(detail.getTotal());
                }
            }
            cart.setTotal(total);
            
            // 6. Save cart again
            cartRepository.save(cart);
            
            // 7. Clear Hibernate session
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
            // If no token provided, try to get it from SecurityContext
            String actualToken = token;
            if (actualToken == null || actualToken.isEmpty()) {
                actualToken = extractTokenFromSecurityContext();
            }
            
            ProductDTO product = getProductDetails(productId, actualToken);
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
    public Map<String, Object> checkout(Long cartId, String token) {
        try {
            log.info("Starting checkout process for cart ID: {}", cartId);
            
            Cart cart = cartRepository.findById(cartId)
                    .orElseThrow(() -> new RuntimeException("Cart not found"));
            
            if (cart.getCartDetails().isEmpty()) {
                throw new RuntimeException("Cannot checkout empty cart");
            }
            
            // Ensure token has correct format
            String actualToken = token;
            if (token.startsWith("Bearer ")) {
                actualToken = token.substring(7);
            }
            
            // Create invoice request
            Map<String, Object> invoiceRequest = new HashMap<>();
            invoiceRequest.put("userId", cart.getUserId());
            invoiceRequest.put("totalAmount", cart.getTotal());
            
            // Build cart items list
            List<Map<String, Object>> items = new ArrayList<>();
            for (CartDetails detail : cart.getCartDetails()) {
                Map<String, Object> item = new HashMap<>();
                item.put("productId", detail.getProductId());
                item.put("quantity", detail.getQuantity());
                
                // Calculate unit price from total and quantity
                BigDecimal unitPrice = detail.getTotal()
                        .divide(BigDecimal.valueOf(detail.getQuantity()), 2, RoundingMode.HALF_UP);
                item.put("price", unitPrice);
                
                items.add(item);
                log.debug("Added item to checkout: productId={}, quantity={}, price={}", 
                        detail.getProductId(), detail.getQuantity(), unitPrice);
            }
            invoiceRequest.put("items", items);
            
            // Call invoice service
            log.info("Calling invoice service to create invoice with {} items", items.size());
            try {
                Map<String, Object> response = webClientBuilder.build()
                        .post()
                        .uri(apiGatewayUrl + "/api/v1/invoices/create-from-cart")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + actualToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(invoiceRequest)
                        .retrieve()
                        .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                        .block();
                
                log.info("Invoice created successfully: {}", response);
                
                // Update product inventory
                updateProductInventory(cart.getCartDetails(), actualToken);
                
                // Clear cart after successful checkout
                clearCart(cartId);
                
                log.info("Checkout completed successfully for cart ID: {}", cartId);
                
                // Create response with invoice ID for payment initiation
                Map<String, Object> result = new HashMap<>();
                result.put("status", "success");
                result.put("message", "Checkout completed successfully");
                
                // Extract invoice ID from response
                if (response != null && response.containsKey("data")) {
                    Map<String, Object> invoiceData = (Map<String, Object>) response.get("data");
                    if (invoiceData.containsKey("id")) {
                        result.put("invoiceId", invoiceData.get("id"));
                    }
                    if (invoiceData.containsKey("totalAmount")) {
                        result.put("totalAmount", invoiceData.get("totalAmount"));
                    }
                }
                
                return result;
            } catch (WebClientResponseException e) {
                log.error("Error response from invoice service: {} - {}", 
                        e.getStatusCode(), e.getResponseBodyAsString(), e);
                throw new RuntimeException("Checkout failed: " + e.getStatusCode() + " - " + e.getMessage());
            }
        } catch (Exception e) {
            log.error("Error during checkout: {}", e.getMessage(), e);
            throw new RuntimeException("Checkout failed: " + e.getMessage());
        }
    }
    
    private void updateProductInventory(List<CartDetails> cartDetails, String token) {
        log.info("Updating product inventory for {} items", cartDetails.size());
        
        for (CartDetails detail : cartDetails) {
            try {
                // Get current product
                ProductDTO product = getProductDetails(detail.getProductId(), token);
                
                // Calculate new quantity
                int newQuantity = product.getQuantity() - detail.getQuantity();
                
                // Ensure quantity doesn't go below zero
                if (newQuantity < 0) newQuantity = 0;
                
                // Update inventory using the dedicated inventory endpoint
                Map<String, Integer> request = new HashMap<>();
                request.put("quantity", newQuantity);
                
                webClientBuilder.build()
                        .patch()
                        .uri(apiGatewayUrl + "/api/products/" + detail.getProductId() + "/inventory")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(request)
                        .retrieve()
                        .bodyToMono(Object.class)
                        .block();
                
                log.debug("Updated inventory for product {}: {} -> {}", 
                        detail.getProductId(), product.getQuantity(), newQuantity);
            } catch (Exception e) {
                log.error("Error updating inventory for product {}: {}", 
                        detail.getProductId(), e.getMessage());
                // Continue with other products even if one fails
            }
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
        log.info("Cart {} cleared successfully", cartId);
    }
    
    private ProductDTO getProductDetails(Long productId, String token) {
        log.debug("Fetching product details for ID: {}", productId);
        
        try {
            // First try to use the internal endpoint which is designed for microservice communication
            WebClient.RequestHeadersSpec<?> internalRequest = webClientBuilder.build()
                    .get()
                    .uri(apiGatewayUrl + "/api/products/internal/" + productId);
                    
            // Add token to internal request if available
            if (token != null && !token.isEmpty()) {
                internalRequest = internalRequest.header(HttpHeaders.AUTHORIZATION, 
                        token.startsWith("Bearer ") ? token : "Bearer " + token);
            }
            
            ProductDTO product = internalRequest.retrieve()
                    .bodyToMono(ProductDTO.class)
                    .onErrorResume(WebClientResponseException.class, error -> {
                        log.warn("Internal product endpoint failed, falling back to public endpoint: {}", 
                                error.getMessage());
                        
                        // Fall back to public endpoint with ResponseObject wrapper
                        WebClient.RequestHeadersSpec<?> request = webClientBuilder.build()
                            .get()
                            .uri(apiGatewayUrl + "/api/products/" + productId);
                                
                        if (token != null && !token.isEmpty()) {
                            request = request.header(HttpHeaders.AUTHORIZATION, 
                                    token.startsWith("Bearer ") ? token : "Bearer " + token);
                        }
                        
                        return request.retrieve()
                            .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                            .map(response -> {
                                if (response != null && response.containsKey("data")) {
                                    ObjectMapper mapper = new ObjectMapper();
                                    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                                    
                                    // Extract product from the ResponseObject structure
                                    Object dataObj = response.get("data");
                                    
                                    // Check if the data is wrapped in an Optional
                                    if (dataObj instanceof Map) {
                                        Map<String, Object> dataMap = (Map<String, Object>) dataObj;
                                        if (dataMap.containsKey("get")) {
                                            dataObj = dataMap.get("get");
                                        }
                                    }
                                    
                                    return mapper.convertValue(dataObj, ProductDTO.class);
                                }
                                throw new RuntimeException("Product data not found in response");
                            });
                    })
                    .block();
            
            log.debug("Successfully retrieved product: {}", product.getProductName());
            return product;
        } catch (Exception e) {
            log.error("Error fetching product details: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to get product details: " + e.getMessage());
        }
    }
} 