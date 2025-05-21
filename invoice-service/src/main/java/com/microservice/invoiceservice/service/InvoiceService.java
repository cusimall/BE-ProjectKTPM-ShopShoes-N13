package com.microservice.invoiceservice.service;

import com.microservice.invoiceservice.dto.InvoiceRequest;
import com.microservice.invoiceservice.dto.InvoiceDetailsRequest;
import com.microservice.invoiceservice.entity.Invoice;
import com.microservice.invoiceservice.entity.InvoiceDetails;
import com.microservice.invoiceservice.repository.InvoiceRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InvoiceService {
    private final InvoiceRepository invoiceRepository;

    @Transactional
    public Invoice createInvoice(InvoiceRequest request) {
        // Create new invoice
        Invoice invoice = new Invoice();
        invoice.setUserId(request.getUserId());
        invoice.setShipAddress(request.getShipAddress());
        invoice.setStatus("PENDING");
        invoice.setOrderDate(LocalDateTime.now());
        
        // Initialize invoice details list
        List<InvoiceDetails> details = new ArrayList<>();
        
        // Create and set up invoice details
        for (InvoiceDetailsRequest detailRequest : request.getInvoiceDetails()) {
            InvoiceDetails detail = new InvoiceDetails();
            detail.setProductId(detailRequest.getProductId());
            detail.setQuantity(detailRequest.getQuantity());
            detail.setDiscount(detailRequest.getDiscount());
            detail.setPrice(detailRequest.getPrice());
            detail.setInvoice(invoice); // Set the invoice reference
            details.add(detail);
        }

        // Calculate total price
        double totalPrice = details.stream()
                .mapToDouble(detail -> detail.getPrice() * detail.getQuantity() * (1 - detail.getDiscount()))
                .sum();
        invoice.setTotalPrice(totalPrice);

        // Set the details list to invoice
        invoice.setInvoiceDetails(details);
        
        // Save the invoice and return only the saved invoice
        Invoice savedInvoice = invoiceRepository.save(invoice);
        return savedInvoice;
    }

    public Invoice getInvoiceById(Long id) {
        return invoiceRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Invoice not found with id: " + id));
    }

    public List<Invoice> getInvoicesByUserId(Long userId) {
        return invoiceRepository.findByUserId(userId);
    }

    public List<Invoice> getInvoicesByStatus(String status) {
        return invoiceRepository.findByStatus(status);
    }

    @Transactional
    public Invoice updateInvoiceStatus(Long id, String status) {
        Invoice invoice = getInvoiceById(id);
        invoice.setStatus(status);
        return invoiceRepository.save(invoice);
    }

    @Transactional
    public void deleteInvoice(Long id) {
        if (!invoiceRepository.existsById(id)) {
            throw new EntityNotFoundException("Invoice not found with id: " + id);
        }
        invoiceRepository.deleteById(id);
    }
} 