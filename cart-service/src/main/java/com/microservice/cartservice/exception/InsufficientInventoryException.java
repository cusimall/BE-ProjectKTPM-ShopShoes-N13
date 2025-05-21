package com.microservice.cartservice.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@Getter
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InsufficientInventoryException extends RuntimeException {
    
    private Long productId;
    private int requestedQuantity;
    private int availableQuantity;
    
    public InsufficientInventoryException(String message) {
        super(message);
    }
    
    public InsufficientInventoryException(Long productId, int requestedQuantity, int availableQuantity) {
        super("Insufficient inventory for product " + productId + 
              ": requested " + requestedQuantity + ", available " + availableQuantity);
        this.productId = productId;
        this.requestedQuantity = requestedQuantity;
        this.availableQuantity = availableQuantity;
    }

} 