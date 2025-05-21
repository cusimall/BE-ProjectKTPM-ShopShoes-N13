package com.microservice.invoiceservice.controller;

import com.microservice.invoiceservice.dto.ApiResponse;
import com.microservice.invoiceservice.dto.InvoiceItemRequest;
import com.microservice.invoiceservice.dto.InvoiceItemResponse;
import com.microservice.invoiceservice.service.InvoiceItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/v1/invoice-items")
@RequiredArgsConstructor
public class InvoiceItemController {
    private final InvoiceItemService invoiceItemService;

    @GetMapping("/invoice/{invoiceId}")
    public ResponseEntity<ApiResponse<List<InvoiceItemResponse>>> getInvoiceItemsByInvoiceId(
            @PathVariable Long invoiceId) {
        List<InvoiceItemResponse> items = invoiceItemService.getInvoiceItemsByInvoiceId(invoiceId);
        ApiResponse<List<InvoiceItemResponse>> response = new ApiResponse<>(
            "Invoice items retrieved successfully!",
            "SUCCESS",
            items
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<ApiResponse<List<InvoiceItemResponse>>> getInvoiceItemsByProductId(
            @PathVariable Long productId) {
        List<InvoiceItemResponse> items = invoiceItemService.getInvoiceItemsByProductId(productId);
        ApiResponse<List<InvoiceItemResponse>> response = new ApiResponse<>(
            "Invoice items retrieved successfully!",
            "SUCCESS",
            items
        );
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<InvoiceItemResponse>> updateInvoiceItem(
            @PathVariable Long id,
            @RequestBody InvoiceItemRequest request) {
        InvoiceItemResponse updatedItem = invoiceItemService.updateInvoiceItem(id, request);
        ApiResponse<InvoiceItemResponse> response = new ApiResponse<>(
            "Invoice item updated successfully!",
            "SUCCESS",
            updatedItem
        );
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteInvoiceItem(@PathVariable Long id) {
        invoiceItemService.deleteInvoiceItem(id);
        ApiResponse<Void> response = new ApiResponse<>(
            "Invoice item deleted successfully!",
            "SUCCESS",
            null
        );
        return ResponseEntity.ok(response);
    }
} 