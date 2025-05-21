package com.microservice.paymentservice.repository;

import com.microservice.paymentservice.models.PaymentTransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentTransactionRepository extends JpaRepository<PaymentTransactionEntity, Long> {
    
    Optional<PaymentTransactionEntity> findByInvoiceId(Long invoiceId);
    
    Optional<PaymentTransactionEntity> findByProviderTransactionId(String providerTransactionId);
    
    List<PaymentTransactionEntity> findByUserId(Long userId);
    
    List<PaymentTransactionEntity> findByStatus(String status);
} 