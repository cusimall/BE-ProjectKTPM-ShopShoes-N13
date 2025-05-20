package com.microservice.invoiceservice.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;


@Entity
@Table(name = "INVOICE_ITEMS")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceItem {
    @Id
    @Column(name = "INVOICE_ITEM_ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "PRODUCT_ID", nullable = false)
    private Long productId;

    @Column(name = "QUANTITY", nullable = false)
    private Integer quantity;

    @Column(name = "PRICE", precision = 19, scale = 2, nullable = false)
    private BigDecimal price;

    @Column(name = "SUBTOTAL", precision = 19, scale = 2, nullable = false)
    private BigDecimal subtotal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "INVOICE_ID", nullable = false)
    @JsonBackReference
    private Invoice invoice;

    @PrePersist
    @PreUpdate
    public void calculateSubtotal() {
        if (quantity != null && price != null) {
            subtotal = price.multiply(BigDecimal.valueOf(quantity));
        }
    }
}