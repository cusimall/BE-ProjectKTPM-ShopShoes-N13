package com.microservice.invoiceservice.dto;

import com.microservice.invoiceservice.entity.InvoiceItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceItemResponse {
    private Long id;
    private BigDecimal price;
    private Integer quantity;
    private BigDecimal subtotal;
    private Long productId;
    private Long invoiceId;

    public static InvoiceItemResponse fromEntity(InvoiceItem item) {
        return InvoiceItemResponse.builder()
                .id(item.getId())
                .price(item.getPrice())
                .quantity(item.getQuantity())
                .subtotal(item.getSubtotal())
                .productId(item.getProductId())
                .invoiceId(item.getInvoice().getId())
                .build();
    }
} 