package com.microservice.paymentservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO representing a payment transaction
 * Used for storing transaction information and communicating with payment provider
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentTransaction {
    /**
     * Internal transaction ID
     */
    private String id;
    
    /**
     * Transaction reference from payment provider
     */
    private String providerTransactionId;
    
    /**
     * Associated invoice ID
     */
    private Long invoiceId;
    
    /**
     * User ID who initiated the payment
     */
    private Long userId;
    
    /**
     * Transaction amount
     */
    private BigDecimal amount;
    
    /**
     * Currency code (e.g., VND)
     */
    private String currencyCode;
    
    /**
     * Current status of the transaction
     */
    private String status;
    
    /**
     * Payment method used
     */
    private String paymentMethod;
    
    /**
     * Bank code (if applicable)
     */
    private String bankCode;
    
    /**
     * Time when transaction was created
     */
    private LocalDateTime createdAt;
    
    /**
     * Time when transaction was last updated
     */
    private LocalDateTime updatedAt;
    
    /**
     * Time when transaction was completed (successfully or not)
     */
    private LocalDateTime completedAt;
    
    /**
     * Error message (if any)
     */
    private String errorMessage;
    
    /**
     * Error code (if any)
     */
    private String errorCode;
    
    /**
     * Raw response data from payment provider
     */
    private String rawResponse;
}