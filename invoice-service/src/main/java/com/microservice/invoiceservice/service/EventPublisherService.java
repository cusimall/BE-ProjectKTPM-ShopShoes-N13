package com.microservice.invoiceservice.service;

import com.microservice.invoiceservice.dto.InvoiceEvent;
import com.microservice.invoiceservice.dto.InvoiceItemDto;
import com.microservice.invoiceservice.entity.Invoice;
import com.microservice.invoiceservice.entity.InvoiceItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class EventPublisherService {
    
    private final RabbitTemplate rabbitTemplate;
    
    public void publishInvoiceCreated(Invoice invoice) {
        InvoiceEvent event = buildInvoiceEvent(invoice, "INVOICE_CREATED");
        publishToExchange(event);
        log.info("Published invoice created event for invoice: {}", invoice.getId());
    }
    
    public void publishInvoiceUpdated(Invoice invoice) {
        InvoiceEvent event = buildInvoiceEvent(invoice, "INVOICE_UPDATED");
        publishToExchange(event);
        log.info("Published invoice updated event for invoice: {}", invoice.getId());
    }
    
    private InvoiceEvent buildInvoiceEvent(Invoice invoice, String eventType) {
        List<InvoiceItemDto> items = mapInvoiceItems(invoice.getItems());
        
        return InvoiceEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType(eventType)
                .eventTime(LocalDateTime.now())
                .invoiceId(invoice.getId())
                .userId(invoice.getUserId())
                .totalAmount(invoice.getTotalAmount())
                .status(invoice.getStatus())
                .createdAt(invoice.getCreatedAt())
                .items(items)
                .build();
    }
    
    private List<InvoiceItemDto> mapInvoiceItems(List<InvoiceItem> items) {
        if (items == null) {
            return List.of();
        }
        
        return items.stream()
                .map(item -> new InvoiceItemDto(
                        item.getProductId(),
                        null, // Product name not available in invoice item
                        item.getQuantity(),
                        item.getPrice()))
                .collect(Collectors.toList());
    }
    
    private void publishToExchange(InvoiceEvent event) {
        try {
            // Publish to invoice events exchange
            rabbitTemplate.convertAndSend("invoice.exchange", "invoice.events", event);
            
            // Publish to email service for notifications
            rabbitTemplate.convertAndSend("email.exchange", "email.invoice.events", event);
            
            log.debug("Invoice event published: {}", event);
        } catch (Exception e) {
            log.error("Failed to publish invoice event: {}", e.getMessage(), e);
        }
    }
} 