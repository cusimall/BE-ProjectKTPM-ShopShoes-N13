package com.microservice.invoiceservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentEvent implements Serializable {
    
    private String eventId;
    private String eventType;
    private LocalDateTime eventTime;
    
    private String transactionId;
    private Long invoiceId;
    private Long userId;
    private BigDecimal amount;
    private String status;
    private String paymentMethod;
    private String errorCode;
    private String errorMessage;
} 