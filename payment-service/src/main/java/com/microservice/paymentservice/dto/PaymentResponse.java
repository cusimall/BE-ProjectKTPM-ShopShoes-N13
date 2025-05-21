package com.microservice.paymentservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for payment response data sent to clients
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {
    /**
     * Status code of the payment response
     */
    private String status;
    
    /**
     * Human-readable message about the payment status
     */
    private String message;
    
    /**
     * URL to redirect user for payment completion (for payment creation)
     */
    private String paymentUrl;
    
    /**
     * Transaction reference ID from payment provider
     */
    private String transactionId;
    
    /**
     * Invoice ID related to this payment
     */
    private Long invoiceId;
    
    /**
     * Amount that was paid
     */
    private BigDecimal amount;
    
    /**
     * Date and time of the payment transaction
     */
    private LocalDateTime transactionDate;
    
    /**
     * Bank code used for the transaction
     */
    private String bankCode;
    
    /**
     * Card type used (if applicable)
     */
    private String cardType;
    
    /**
     * Error code (if any)
     */
    private String errorCode;
}
