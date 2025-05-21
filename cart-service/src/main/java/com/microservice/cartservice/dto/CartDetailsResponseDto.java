package com.microservice.cartservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartDetailsResponseDto {
    private Long cartDetailsId;
    private Long productId;
    private Integer quantity;
    private BigDecimal total;
    private ProductDTO product;
} 