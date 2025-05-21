package com.microservice.emailsenderservice.dto;

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
    
    private List<InvoiceItemDto> items;
    
    // Additional information for email service
    private String customerEmail;
    private String customerName;
    private String shippingAddress;
}

//@Data
//@NoArgsConstructor
//@AllArgsConstructor
//class InvoiceItemDto implements Serializable {
//    private Long productId;
//    private String productName;
//    private Integer quantity;
//    private BigDecimal price;
//}