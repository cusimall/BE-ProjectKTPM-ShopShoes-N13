package com.microservice.cartservice.controller;

import com.microservice.cartservice.dto.CartRequest;
import com.microservice.cartservice.models.Cart;
import com.microservice.cartservice.models.CartDetails;
import com.microservice.cartservice.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/carts")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;
    
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
    
    private String getTokenValue(String authHeader) {
        // Remove "Bearer " prefix if present
        return authHeader.startsWith("Bearer ") ? 
                authHeader.substring(7) : authHeader;
    }
} 