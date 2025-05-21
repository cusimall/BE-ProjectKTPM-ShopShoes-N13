package com.microservice.cartservice.controller;

import com.microservice.cartservice.dto.CartRequest;
import com.microservice.cartservice.models.Cart;
import com.microservice.cartservice.models.CartDetails;
import com.microservice.cartservice.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;

import jakarta.validation.Valid;
import java.util.HashMap;

@RestController
@RequestMapping("/api/carts")
@RequiredArgsConstructor
@Slf4j
public class CartController {

    private final CartService cartService;
    private final WebClient.Builder webClientBuilder;
    @Value("${api.gateway.url}")
    private String apiGatewayUrl;
    
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Cart> getUserCart(@PathVariable Long userId) {
        return ResponseEntity.ok(cartService.getOrCreateCart(userId));
    }
    
    @PostMapping("/user/{userId}/items")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Cart> addToCart(
            @PathVariable Long userId,
            @Valid @RequestBody CartRequest request,
            @RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(cartService.addToCart(userId, request, getTokenValue(token)));
    }
    
    @GetMapping("/{cartId}/items")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<CartDetails>> getCartDetails(@PathVariable Long cartId) {
        return ResponseEntity.ok(cartService.getCartDetails(cartId));
    }
    
    @DeleteMapping("/{cartId}/items/{productId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> removeFromCart(
            @PathVariable Long cartId,
            @PathVariable Long productId) {
        cartService.removeFromCart(cartId, productId);
        return ResponseEntity.ok().build();
    }
    
    @PatchMapping("/{cartId}/items/{productId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<CartDetails> updateQuantity(
            @PathVariable Long cartId,
            @PathVariable Long productId,
            @RequestParam int quantity,
            @RequestHeader("Authorization") String token) {
        CartDetails detail = cartService.updateQuantity(cartId, productId, quantity, getTokenValue(token));
        return ResponseEntity.ok(detail);
    }
    
    @PostMapping("/{cartId}/checkout")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> checkout(
            @PathVariable Long cartId,
            @RequestHeader("Authorization") String token) {
        cartService.checkout(cartId, getTokenValue(token));
        return ResponseEntity.ok().build();
    }
    
    @DeleteMapping("/{cartId}/clear")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> clearCart(@PathVariable Long cartId) {
        cartService.clearCart(cartId);
        return ResponseEntity.ok().build();
    }

    
    @GetMapping("/invoice/{invoiceId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> getInvoiceDetails(
            @PathVariable Long invoiceId,
            @RequestHeader("Authorization") String token) {
        try {
            // Add "v1/" to the path
            Object invoiceDetails = webClientBuilder.build()
                    .get()
                    .uri(apiGatewayUrl + "/api/v1/invoices/" + invoiceId) // CORRECTED PATH WITH v1/
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + getTokenValue(token))
                    .retrieve()
                    .bodyToMono(Object.class)
                    .block();
            
            return ResponseEntity.ok(invoiceDetails);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to get invoice: " + e.getMessage()));
        }
    }
    
    @GetMapping("/test-invoice")
    public ResponseEntity<?> testInvoiceService() {
        try {
            String result = webClientBuilder.build()
                    .get()
                    .uri(apiGatewayUrl + "/api/v1/invoices/test")
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
                    
            return ResponseEntity.ok(Map.of("result", result));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                        "error", e.getMessage(),
                        "type", e.getClass().getName()
                    ));
        }
    }
    
    private String getTokenValue(String authHeader) {
        // Remove "Bearer " prefix if present
        return authHeader.startsWith("Bearer ") ? 
                authHeader.substring(7) : authHeader;
    }
} 