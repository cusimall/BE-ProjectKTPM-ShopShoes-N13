package com.microservice.paymentservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for payment request data coming from clients
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequest {
    /**
     * Invoice ID to be paid
     */
    private Long invoiceId;
    
    /**
     * Amount to be paid
     */
    private BigDecimal amount;
    
    /**
     * Bank code (optional) for directed payments
     */
    private String bankCode;
    
    /**
     * Return URL where user will be redirected after payment
     * (Optional, can override default configuration)
     */
    private String returnUrl;
    
    /**
     * Description or note for the payment
     */
    private String description;
    
    /**
     * Language for payment page (default: 'vn')
     */
    private String language;
}
