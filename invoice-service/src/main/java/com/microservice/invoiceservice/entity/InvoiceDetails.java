package com.microservice.invoiceservice.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "INVOICE_DETAILS")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceDetails {
    @Id
    @Column(name = "INVOICE_DETAILS_ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long invoiceDetailsId;

    @Column(name = "DISCOUNT")
    private Double discount;

    @Column(name = "PRICE")
    private Double price;

    @Column(name = "QUANTITY")
    private Integer quantity;

    @Column(name = "PRODUCT_ID")
    private Long productId;

    @ManyToOne
    @JoinColumn(name = "INVOICE_ID")
    @JsonBackReference
    private Invoice invoice;
} 