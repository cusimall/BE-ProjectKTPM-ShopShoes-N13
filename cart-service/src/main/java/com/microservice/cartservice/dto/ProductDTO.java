package com.microservice.cartservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO {
    private Long id;
    private String productName;
    private String description;
    private String category;
    private BigDecimal productPrice;
    private String imgUrl;
    private Integer quantity;
    private String brandName;
    private String designer;
    private Integer reorderLevel;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean inStock;
    private Boolean lowStock;
} 