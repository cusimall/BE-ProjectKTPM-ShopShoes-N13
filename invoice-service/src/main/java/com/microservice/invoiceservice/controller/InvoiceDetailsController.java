package com.microservice.invoiceservice.controller;

import com.microservice.invoiceservice.dto.ApiResponse;
import com.microservice.invoiceservice.dto.InvoiceDetailsRequest;
import com.microservice.invoiceservice.dto.InvoiceDetailsResponse;
import com.microservice.invoiceservice.service.InvoiceDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/invoice-details")
@RequiredArgsConstructor
public class InvoiceDetailsController {
    private final InvoiceDetailsService invoiceDetailsService;

    @GetMapping("/invoice/{invoiceId}")
    public ResponseEntity<ApiResponse<List<InvoiceDetailsResponse>>> getInvoiceDetailsByInvoiceId(
            @PathVariable Long invoiceId) {
        List<InvoiceDetailsResponse> details = invoiceDetailsService.getInvoiceDetailsByInvoiceId(invoiceId);
        ApiResponse<List<InvoiceDetailsResponse>> response = new ApiResponse<>(
            "Invoice details retrieved successfully!",
            "SUCCESS",
            details
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<ApiResponse<List<InvoiceDetailsResponse>>> getInvoiceDetailsByProductId(
            @PathVariable Long productId) {
        List<InvoiceDetailsResponse> details = invoiceDetailsService.getInvoiceDetailsByProductId(productId);
        ApiResponse<List<InvoiceDetailsResponse>> response = new ApiResponse<>(
            "Invoice details retrieved successfully!",
            "SUCCESS",
            details
        );
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<InvoiceDetailsResponse>> updateInvoiceDetails(
            @PathVariable Long id,
            @RequestBody InvoiceDetailsRequest request) {
        InvoiceDetailsResponse updatedDetails = invoiceDetailsService.updateInvoiceDetails(id, request);
        ApiResponse<InvoiceDetailsResponse> response = new ApiResponse<>(
            "Invoice details updated successfully!",
            "SUCCESS",
            updatedDetails
        );
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteInvoiceDetails(@PathVariable Long id) {
        invoiceDetailsService.deleteInvoiceDetails(id);
        ApiResponse<Void> response = new ApiResponse<>(
            "Invoice details deleted successfully!",
            "SUCCESS",
            null
        );
        return ResponseEntity.ok(response);
    }
} 