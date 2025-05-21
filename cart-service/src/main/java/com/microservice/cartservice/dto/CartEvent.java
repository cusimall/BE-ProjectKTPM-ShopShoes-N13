package com.microservice.cartservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartEvent implements Serializable {
    private String eventId;
    private String eventType;
    private LocalDateTime eventTime;
    
    private Long cartId;
    private Long userId;
    private BigDecimal total;
    
    @Builder.Default
    private List<CartEventItem> items = new ArrayList<>();
} 