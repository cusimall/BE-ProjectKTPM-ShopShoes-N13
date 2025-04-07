package com.microservice.cartservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceDTO {
    private Long userId;
    private BigDecimal totalAmount;
    private LocalDateTime createdDate;
    private List<InvoiceItemDTO> items;
} 