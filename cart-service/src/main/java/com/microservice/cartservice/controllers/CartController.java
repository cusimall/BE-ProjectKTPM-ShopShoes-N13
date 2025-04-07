package com.microservice.cartservice.controllers;

import com.microservice.cartservice.dto.CartRequest;
import com.microservice.cartservice.models.Cart;
import com.microservice.cartservice.models.CartDetails;
import com.microservice.cartservice.services.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {
    private final CartService cartService;

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Cart> getOrCreateCart(@PathVariable Long userId, @RequestHeader("Authorization") String authToken) {
        Cart cart = cartService.getOrCreateCart(userId, authToken.replace("Bearer ", ""));
        return ResponseEntity.ok(cart);
    }

    @PostMapping("/{cartId}/addProduct")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> addProductToCart(
            @PathVariable Long cartId,
            @Valid @RequestBody CartRequest request,
            @RequestHeader("Authorization") String authToken) {
        Cart cart = cartService.addProductToCart(cartId, request.getProductId(), request.getQuantity(), 
                authToken.replace("Bearer ", ""));
        return ResponseEntity.ok(cart);
    }

    @DeleteMapping("/{cartId}/products/{productId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> removeProductFromCart(@PathVariable Long cartId, @PathVariable Long productId) {
        cartService.removeProductFromCart(cartId, productId);
        return ResponseEntity.ok("Product removed from cart");
    }

    @GetMapping("/{cartId}/products")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<CartDetails>> getCart(@PathVariable Long cartId) {
        List<CartDetails> cartDetails = cartService.getAllCartDetails(cartId);
        return ResponseEntity.ok(cartDetails);
    }

    @PostMapping("/{cartId}/products/{productId}/increase")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> increaseCartItemQuantity(@PathVariable Long cartId, @PathVariable Long productId) {
        cartService.increaseCartItemQuantity(cartId, productId);
        return ResponseEntity.ok("Cart item quantity increased");
    }

    @PostMapping("/{cartId}/products/{productId}/decrease")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> decreaseCartItemQuantity(@PathVariable Long cartId, @PathVariable Long productId) {
        cartService.decreaseCartItemQuantity(cartId, productId);
        return ResponseEntity.ok("Cart item quantity decreased");
    }

    @PostMapping("/{cartId}/checkout")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> checkout(@PathVariable Long cartId, @RequestHeader("Authorization") String authToken) {
        cartService.createInvoiceFromCart(cartId, authToken.replace("Bearer ", ""));
        return ResponseEntity.ok("Checkout successful");
    }
}
