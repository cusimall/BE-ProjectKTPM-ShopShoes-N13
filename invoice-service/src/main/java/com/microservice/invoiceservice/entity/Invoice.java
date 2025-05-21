package com.microservice.invoiceservice.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "INVOICE")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Invoice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "INVOICE_ID")
    private Long invoiceId;

    @Column(name = "ORDER_DATE")
    private LocalDateTime orderDate;

    @Column(name = "SHIP_ADDRESS")
    private String shipAddress;

    @Column(name = "STATUS")
    private String status;

    @Column(name = "TOTAL_PRICE")
    private Double totalPrice;

    @Column(name = "USER_ID")
    private Long userId;

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<InvoiceDetails> invoiceDetails = new ArrayList<>();

    public void addInvoiceDetail(InvoiceDetails detail) {
        invoiceDetails.add(detail);
        detail.setInvoice(this);
    }

    public void removeInvoiceDetail(InvoiceDetails detail) {
        invoiceDetails.remove(detail);
        detail.setInvoice(null);
    }
} 