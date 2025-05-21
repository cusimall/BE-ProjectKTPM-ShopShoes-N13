package com.microservice.cartservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class CartNotFoundException extends RuntimeException {
    
    public CartNotFoundException(String message) {
        super(message);
    }
    
    public CartNotFoundException(Long cartId) {
        super("Cart not found with id: " + cartId);
    }
    
    public CartNotFoundException(Long userId, String message) {
        super("Cart not found for user " + userId + ": " + message);
    }
} 