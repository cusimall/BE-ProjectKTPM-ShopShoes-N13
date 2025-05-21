package com.microservice.invoiceservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceEvent implements Serializable {
    
    private String eventId;
    private String eventType;
    private LocalDateTime eventTime;
    
    private Long invoiceId;
    private Long userId;
    private BigDecimal totalAmount;
    private String status;
    private LocalDateTime createdAt;
    private String shipAddress;
    
    private List<InvoiceItemDto> items;
}