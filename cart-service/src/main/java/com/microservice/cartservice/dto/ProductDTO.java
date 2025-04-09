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
    private String description;
    private BigDecimal productPrice;
    private String category;
    private String brandName;
    private String imgUrl;
    private String designer;
    private Integer quantity;
} 