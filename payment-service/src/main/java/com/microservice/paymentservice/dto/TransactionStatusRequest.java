package com.microservice.paymentservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for requesting transaction status from payment provider
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionStatusRequest {
    /**
     * Transaction reference ID from payment provider
     */
    private String transactionRef;
    
    /**
     * Invoice ID in our system
     */
    private Long invoiceId;
    
    /**
     * Order info provided during payment creation
     */
    private String orderInfo;
    
    /**
     * Include detailed payment information in response
     */
    private boolean includeDetails;
}