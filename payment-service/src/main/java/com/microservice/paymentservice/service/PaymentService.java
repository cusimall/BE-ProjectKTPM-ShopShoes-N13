package com.microservice.paymentservice.service;

import com.microservice.paymentservice.dto.PaymentTransaction;
import java.util.Map;

public interface PaymentService {
    
    /**
     * Store a new transaction for later verification
     * @param invoiceId invoice ID
     * @param transactionRef transaction reference from payment provider
     * @return The created transaction record
     */
    PaymentTransaction storeTransaction(Long invoiceId, String transactionRef);
    
    /**
     * Validate a transaction callback from payment provider
     * @param params callback parameters
     * @return true if valid, false otherwise
     */
    boolean validateTransaction(Map<String, String> params);
    
    /**
     * Update invoice status after payment
     * @param invoiceId invoice ID
     * @param successful payment success status
     * @return The updated transaction record
     */
    PaymentTransaction updateInvoiceStatus(Long invoiceId, boolean successful);
    
    /**
     * Get payment status for an invoice
     * @param invoiceId invoice ID
     * @return payment transaction details
     */
    PaymentTransaction getPaymentStatus(Long invoiceId);
}