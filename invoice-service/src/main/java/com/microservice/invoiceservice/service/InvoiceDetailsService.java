package com.microservice.invoiceservice.service;

import com.microservice.invoiceservice.dto.InvoiceDetailsRequest;
import com.microservice.invoiceservice.dto.InvoiceDetailsResponse;
import com.microservice.invoiceservice.entity.Invoice;
import com.microservice.invoiceservice.entity.InvoiceDetails;
import com.microservice.invoiceservice.repository.InvoiceDetailsRepository;
import com.microservice.invoiceservice.repository.InvoiceRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InvoiceDetailsService {
    private final InvoiceDetailsRepository invoiceDetailsRepository;
    private final InvoiceRepository invoiceRepository;

    public List<InvoiceDetailsResponse> getInvoiceDetailsByInvoiceId(Long invoiceId) {
        List<InvoiceDetails> details = invoiceDetailsRepository.findByInvoice_InvoiceId(invoiceId);
        return details.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public List<InvoiceDetailsResponse> getInvoiceDetailsByProductId(Long productId) {
        List<InvoiceDetails> details = invoiceDetailsRepository.findByProductId(productId);
        return details.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public InvoiceDetailsResponse updateInvoiceDetails(Long id, InvoiceDetailsRequest request) {
        InvoiceDetails details = invoiceDetailsRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Invoice details not found with id: " + id));

        details.setQuantity(request.getQuantity());
        details.setDiscount(request.getDiscount());
        details.setPrice(request.getPrice());
        details.setProductId(request.getProductId());

        InvoiceDetails updatedDetails = invoiceDetailsRepository.save(details);
        return convertToResponse(updatedDetails);
    }

    @Transactional
    public void deleteInvoiceDetails(Long id) {
        if (!invoiceDetailsRepository.existsById(id)) {
            throw new EntityNotFoundException("Invoice details not found with id: " + id);
        }
        invoiceDetailsRepository.deleteById(id);
    }

    private InvoiceDetailsResponse convertToResponse(InvoiceDetails details) {
        return new InvoiceDetailsResponse(
                details.getInvoiceDetailsId(),
                details.getDiscount(),
                details.getPrice(),
                details.getQuantity(),
                details.getProductId(),
                details.getInvoice().getInvoiceId()
        );
    }
} 