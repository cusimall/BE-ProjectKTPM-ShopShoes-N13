package com.microservice.invoiceservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceRequest {
    private Long userId;
    private String shipAddress;
    private List<InvoiceDetailsRequest> invoiceDetails;
} 