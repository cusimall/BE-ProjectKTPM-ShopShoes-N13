package com.microservice.invoiceservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceRequest {
    private Long userId;
    private BigDecimal totalAmount;
    private String status;
    private String shipAddress;
    private List<Map<String, Object>> items;
} 