package com.microservice.cartservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceItemDTO {
    private Long productId;
    private String productName;
    private int quantity;
    private BigDecimal price;
    private BigDecimal total;
} 