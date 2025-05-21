package com.microservice.cartservice.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@Getter
@ResponseStatus(HttpStatus.FORBIDDEN)
public class UnauthorizedAccessException extends RuntimeException {
    
    private Long cartId;
    private Long userId;
    
    public UnauthorizedAccessException(String message) {
        super(message);
    }
    
    public UnauthorizedAccessException(Long userId, Long cartId) {
        super("User " + userId + " not authorized to access cart " + cartId);
        this.userId = userId;
        this.cartId = cartId;
    }

} 