package com.microservice.cartservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO {
    private Long id;
    private String productName;
    private String productDescription;
    private BigDecimal productPrice;
    private Long categoryId;
    private String imageUrl;
    private int quantity;
} 