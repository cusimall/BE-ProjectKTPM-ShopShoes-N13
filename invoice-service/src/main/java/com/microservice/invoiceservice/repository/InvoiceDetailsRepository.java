package com.microservice.invoiceservice.repository;

import com.microservice.invoiceservice.entity.InvoiceDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InvoiceDetailsRepository extends JpaRepository<InvoiceDetails, Long> {
    List<InvoiceDetails> findByInvoice_InvoiceId(Long invoiceId);
    List<InvoiceDetails> findByProductId(Long productId);
} 