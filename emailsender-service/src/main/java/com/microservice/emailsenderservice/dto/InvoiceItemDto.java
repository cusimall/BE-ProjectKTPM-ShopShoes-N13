package com.microservice.emailsenderservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceItemDto implements Serializable {
    private Long productId;
    private String productName;
    private Integer quantity;
    private BigDecimal price;
}
