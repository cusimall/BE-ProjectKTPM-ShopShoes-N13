package com.microservice.paymentservice.service.impl;

import com.microservice.paymentservice.config.VNPayConfig;
import com.microservice.paymentservice.dto.PaymentTransaction;
import com.microservice.paymentservice.models.PaymentTransactionEntity;
import com.microservice.paymentservice.repository.PaymentTransactionRepository;
import com.microservice.paymentservice.service.PaymentEventPublisher;
import com.microservice.paymentservice.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class VNPayServiceImpl implements PaymentService {
    
    private final VNPayConfig vnPayConfig;
    private final WebClient.Builder webClientBuilder;
    private final PaymentTransactionRepository transactionRepository;
    private final PaymentEventPublisher eventPublisher;
    
    @Value("${api.gateway.url}")
    private String apiGatewayUrl;
    
    @Override
    @Transactional
    public PaymentTransaction storeTransaction(Long invoiceId, String transactionRef) {
        log.info("Storing transaction reference {} for invoice {}", transactionRef, invoiceId);
        
        // Fetch invoice data to get user ID and amount
        Long userId = null;
        BigDecimal amount = null;
        
        try {
            Map<String, Object> response = webClientBuilder.build()
                    .get()
                    .uri(apiGatewayUrl + "/api/v1/invoices/" + invoiceId)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                    .block();
            
            if (response != null && response.containsKey("data")) {
                Map<String, Object> data = (Map<String, Object>) response.get("data");
                userId = Long.valueOf(data.get("userId").toString());
                amount = new BigDecimal(data.get("totalAmount").toString());
            }
        } catch (Exception e) {
            log.error("Error fetching invoice data: {}", e.getMessage());
            // Continue with null values if invoice service is not available
        }
        
        // Create entity
        PaymentTransactionEntity entity = PaymentTransactionEntity.builder()
                .invoiceId(invoiceId)
                .userId(userId)
                .providerTransactionId(transactionRef)
                .status("PENDING")
                .paymentMethod("VNPAY")
                .currencyCode("VND")
                .amount(amount)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        // Save to database
        entity = transactionRepository.save(entity);
        
        // Publish event
        eventPublisher.publishPaymentInitialized(entity);
        
        // Convert to DTO
        PaymentTransaction transaction = PaymentTransaction.builder()
                .id(entity.getTransactionId())
                .invoiceId(entity.getInvoiceId())
                .providerTransactionId(entity.getProviderTransactionId())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .currencyCode(entity.getCurrencyCode())
                .paymentMethod(entity.getPaymentMethod())
                .amount(entity.getAmount())
                .build();
        
        log.debug("Transaction stored: {}", transaction);
        return transaction;
    }
    
    @Override
    @Transactional
    public boolean validateTransaction(Map<String, String> params) {
        // Create a copy of params excluding vnp_SecureHash
        Map<String, String> vnpParams = new HashMap<>();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (!entry.getKey().equals("vnp_SecureHash") && !entry.getKey().equals("vnp_SecureHashType") && 
                !entry.getKey().equals("invoiceId")) {
                vnpParams.put(entry.getKey(), entry.getValue());
            }
        }
        
        // Calculate secure hash from params
        String secureHash = vnPayConfig.hashAllFields(vnpParams);
        String vnpSecureHash = params.get("vnp_SecureHash");
        
        log.info("Validating transaction - calculated hash: {}, received hash: {}", secureHash, vnpSecureHash);
        
        // Validate hash
        boolean isValid = secureHash.equals(vnpSecureHash);
        
        // Validate vnp_TxnRef exists in our database
        Long invoiceId = Long.valueOf(params.get("invoiceId"));
        if (isValid) {
            String vnpTxnRef = params.get("vnp_TxnRef");
            Optional<PaymentTransactionEntity> transaction = transactionRepository.findByProviderTransactionId(vnpTxnRef);
            if (transaction.isPresent()) {
                isValid = transaction.get().getInvoiceId().equals(invoiceId);
            } else {
                isValid = false;
            }
        }
        
        return isValid;
    }
    
    @Override
    @Transactional
    public PaymentTransaction updateInvoiceStatus(Long invoiceId, boolean successful) {
        log.info("Updating invoice {} status to {}", invoiceId, successful ? "PAID" : "PAYMENT_FAILED");
        
        // Get existing transaction
        PaymentTransactionEntity transaction = transactionRepository.findByInvoiceId(invoiceId)
                .orElseGet(() -> {
                    log.warn("No transaction found for invoice {}, creating new record", invoiceId);
                    return PaymentTransactionEntity.builder()
                            .invoiceId(invoiceId)
                            .status(successful ? "PAID" : "PAYMENT_FAILED")
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .completedAt(LocalDateTime.now())
                            .currencyCode("VND")
                            .paymentMethod("VNPAY")
                            .build();
                });
        
        // Update transaction
        transaction.setStatus(successful ? "PAID" : "PAYMENT_FAILED");
        transaction.setUpdatedAt(LocalDateTime.now());
        transaction.setCompletedAt(LocalDateTime.now());
        transaction = transactionRepository.save(transaction);
        
        // Publish event based on success/failure
        if (successful) {
            eventPublisher.publishPaymentCompleted(transaction);
        } else {
            transaction.setErrorMessage("Payment failed");
            eventPublisher.publishPaymentFailed(transaction);
        }
        
        try {
            // Call invoice service to update status
            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("status", successful ? "PAID" : "PAYMENT_FAILED");
            requestBody.put("paymentId", transaction.getTransactionId());
            
            Map<String, Object> response = webClientBuilder.build()
                    .put()
                    .uri(apiGatewayUrl + "/api/v1/invoices/" + invoiceId + "/status")
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                    .block();
            
            log.info("Updated invoice {} status to {}", invoiceId, successful ? "PAID" : "PAYMENT_FAILED");
            
            // Update transaction with amount from response if available
            if (response != null && response.containsKey("data")) {
                Map<String, Object> data = (Map<String, Object>) response.get("data");
                if (data.containsKey("totalAmount")) {
                    String totalAmount = data.get("totalAmount").toString();
                    transaction.setAmount(new BigDecimal(totalAmount));
                    transactionRepository.save(transaction);
                }
            }
        } catch (WebClientResponseException e) {
            log.error("Failed to update invoice status: {} - {}", e.getStatusCode(), e.getResponseBodyAsString(), e);
            transaction.setErrorMessage("Failed to update invoice: " + e.getMessage());
            transactionRepository.save(transaction);
        } catch (Exception e) {
            log.error("Failed to update invoice status: {}", e.getMessage(), e);
            transaction.setErrorMessage("Failed to update invoice: " + e.getMessage());
            transactionRepository.save(transaction);
        }
        
        return mapEntityToDto(transaction);
    }
    
    @Override
    public PaymentTransaction getPaymentStatus(Long invoiceId) {
        log.info("Getting payment status for invoice {}", invoiceId);
        
        // Check if we have the transaction in our database
        Optional<PaymentTransactionEntity> transactionOpt = transactionRepository.findByInvoiceId(invoiceId);
        if (transactionOpt.isPresent()) {
            PaymentTransactionEntity transaction = transactionOpt.get();
            log.debug("Found transaction in database: {}", transaction);
            return mapEntityToDto(transaction);
        }
        
        // If not in our database, try to get from invoice service
        try {
            Map<String, Object> response = webClientBuilder.build()
                    .get()
                    .uri(apiGatewayUrl + "/api/v1/invoices/" + invoiceId)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                    .block();
            
            if (response != null && response.containsKey("data")) {
                Map<String, Object> data = (Map<String, Object>) response.get("data");
                
                // Create transaction from invoice data
                PaymentTransactionEntity transaction = PaymentTransactionEntity.builder()
                        .invoiceId(invoiceId)
                        .status(data.get("status").toString())
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build();
                
                // Extract user ID if available
                if (data.containsKey("userId")) {
                    String userId = data.get("userId").toString();
                    transaction.setUserId(Long.valueOf(userId));
                }
                
                // Extract amount if available
                if (data.containsKey("totalAmount")) {
                    String totalAmount = data.get("totalAmount").toString();
                    transaction.setAmount(new BigDecimal(totalAmount));
                }
                
                // Save to database for future reference
                transaction = transactionRepository.save(transaction);
                
                log.info("Created transaction from invoice data: {}", transaction);
                return mapEntityToDto(transaction);
            } else {
                log.warn("No invoice data found for ID {}", invoiceId);
                
                // Return default transaction with UNKNOWN status
                PaymentTransactionEntity transaction = PaymentTransactionEntity.builder()
                        .invoiceId(invoiceId)
                        .status("UNKNOWN")
                        .errorMessage("No payment information found")
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build();
                
                // Save to database
                transaction = transactionRepository.save(transaction);
                
                return mapEntityToDto(transaction);
            }
        } catch (WebClientResponseException e) {
            log.error("Error getting invoice status: {} - {}", e.getStatusCode(), e.getResponseBodyAsString(), e);
            
            // Create error transaction
            PaymentTransactionEntity transaction = PaymentTransactionEntity.builder()
                    .invoiceId(invoiceId)
                    .status("ERROR")
                    .errorMessage("Failed to get payment status: " + e.getMessage())
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            
            // Save to database
            transaction = transactionRepository.save(transaction);
            
            return mapEntityToDto(transaction);
        } catch (Exception e) {
            log.error("Error getting invoice status: {}", e.getMessage(), e);
            
            // Create error transaction
            PaymentTransactionEntity transaction = PaymentTransactionEntity.builder()
                    .invoiceId(invoiceId)
                    .status("ERROR")
                    .errorMessage("Failed to get payment status: " + e.getMessage())
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            
            // Save to database
            transaction = transactionRepository.save(transaction);
            
            return mapEntityToDto(transaction);
        }
    }
    
    /**
     * Helper method to map entity to DTO
     */
    private PaymentTransaction mapEntityToDto(PaymentTransactionEntity entity) {
        return PaymentTransaction.builder()
                .id(entity.getTransactionId())
                .invoiceId(entity.getInvoiceId())
                .providerTransactionId(entity.getProviderTransactionId())
                .bankCode(entity.getBankCode())
                .amount(entity.getAmount())
                .currencyCode(entity.getCurrencyCode())
                .status(entity.getStatus())
                .errorMessage(entity.getErrorMessage())
                .errorCode(entity.getErrorCode())
                .paymentMethod(entity.getPaymentMethod())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .completedAt(entity.getCompletedAt())
                .build();
    }
}