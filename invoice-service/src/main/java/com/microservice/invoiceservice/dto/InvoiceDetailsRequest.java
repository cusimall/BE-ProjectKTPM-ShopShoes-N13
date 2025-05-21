package com.microservice.invoiceservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceDetailsRequest {
    private Double discount;
    private Double price;
    private Integer quantity;
    private Long productId;
} 