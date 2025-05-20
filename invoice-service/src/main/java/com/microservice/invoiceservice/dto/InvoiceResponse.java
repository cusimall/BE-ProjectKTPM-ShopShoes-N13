package com.microservice.invoiceservice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.microservice.invoiceservice.entity.Invoice;
import com.microservice.invoiceservice.entity.InvoiceItem;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InvoiceResponse {
    private Long id;
    private LocalDateTime orderDate;
    private String shipAddress;
    private String status;
    private String transactionId;
    private BigDecimal totalAmount;
    private Long userId;
    private LocalDateTime createdAt;
    private List<InvoiceItemResponse> items;

    public static InvoiceResponse fromEntity(Invoice invoice) {
        InvoiceResponse response = new InvoiceResponse();
        response.setId(invoice.getId());
        response.setOrderDate(invoice.getOrderDate());
        response.setShipAddress(invoice.getShipAddress());
        response.setStatus(invoice.getStatus());
        response.setTransactionId(invoice.getTransactionId());
        response.setTotalAmount(invoice.getTotalAmount());
        response.setUserId(invoice.getUserId());
        response.setCreatedAt(invoice.getCreatedAt());
        
        if (invoice.getItems() != null) {
            response.setItems(invoice.getItems().stream()
                    .map(InvoiceItemResponse::fromEntity)
                    .collect(Collectors.toList()));
        }
        
        return response;
    }
} 