package com.microservice.cartservice.service;

import com.microservice.cartservice.dto.CartRequest;
import com.microservice.cartservice.models.Cart;
import com.microservice.cartservice.models.CartDetails;

import java.util.List;

public interface CartService {
    Cart getOrCreateCart(Long userId);
    Cart addToCart(Long userId, CartRequest request, String token);
    List<CartDetails> getCartDetails(Long cartId);
    void removeFromCart(Long cartId, Long productId);
    CartDetails updateQuantity(Long cartId, Long productId, int quantity, String token);
    void checkout(Long cartId, String token);
    void clearCart(Long cartId);
} 