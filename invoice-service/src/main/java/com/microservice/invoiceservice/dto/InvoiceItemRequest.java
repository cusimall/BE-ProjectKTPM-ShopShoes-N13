package com.microservice.invoiceservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceItemRequest {
    private Long productId;
    private Integer quantity;
    private BigDecimal price;
}