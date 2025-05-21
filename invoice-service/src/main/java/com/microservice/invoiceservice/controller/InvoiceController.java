package com.microservice.invoiceservice.controller;

import com.microservice.invoiceservice.dto.ApiResponse;
import com.microservice.invoiceservice.dto.InvoiceRequest;
import com.microservice.invoiceservice.entity.Invoice;
import com.microservice.invoiceservice.service.InvoiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/v1/invoices")
@RequiredArgsConstructor
public class InvoiceController {
    private final InvoiceService invoiceService;

    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<Invoice>> createInvoice(@RequestBody InvoiceRequest request) {
        Invoice savedInvoice = invoiceService.createInvoice(request);
        ApiResponse<Invoice> response = new ApiResponse<>(
            "Invoice created successfully!",
            "SUCCESS",
            savedInvoice
        );
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Invoice>> getInvoiceById(@PathVariable Long id) {
        Invoice invoice = invoiceService.getInvoiceById(id);
        ApiResponse<Invoice> response = new ApiResponse<>(
            "Invoice retrieved successfully!",
            "SUCCESS",
            invoice
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<Invoice>>> getInvoicesByUserId(@PathVariable Long userId) {
        List<Invoice> invoices = invoiceService.getInvoicesByUserId(userId);
        ApiResponse<List<Invoice>> response = new ApiResponse<>(
            "Invoices retrieved successfully!",
            "SUCCESS",
            invoices
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<List<Invoice>>> getInvoicesByStatus(@PathVariable String status) {
        List<Invoice> invoices = invoiceService.getInvoicesByStatus(status);
        ApiResponse<List<Invoice>> response = new ApiResponse<>(
            "Invoices retrieved successfully!",
            "SUCCESS",
            invoices
        );
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<Invoice>> updateInvoiceStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        Invoice updatedInvoice = invoiceService.updateInvoiceStatus(id, request.get("status"));
        ApiResponse<Invoice> response = new ApiResponse<>(
            "Invoice status updated successfully!",
            "SUCCESS",
            updatedInvoice
        );
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteInvoice(@PathVariable Long id) {
        invoiceService.deleteInvoice(id);
        ApiResponse<Void> response = new ApiResponse<>(
            "Invoice deleted successfully!",
            "SUCCESS",
            null
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/create-from-cart")
    public ResponseEntity<?> createFromCart(@RequestBody Map<String, Object> request,
                                           @RequestHeader("Authorization") String token) {
        try {
            // Xử lý dữ liệu đầu vào
            Long cartId = Long.valueOf(request.get("cartId").toString());
            Long userId = Long.valueOf(request.get("userId").toString());
            
            // Log để debug
            System.out.println("Received request to create invoice from cart: " + cartId + " for user: " + userId);
            
            // Tạm thời trả về thành công
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Invoice created successfully");
            response.put("invoiceId", 1); // Giả lập ID
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("Error creating invoice: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    // Endpoint test đơn giản
    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("Invoice service is working!");
    }
} 