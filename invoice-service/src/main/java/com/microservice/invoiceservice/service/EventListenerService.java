package com.microservice.invoiceservice.service;

import com.microservice.invoiceservice.dto.PaymentEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class EventListenerService {
    
    private final InvoiceService invoiceService;
    
    @RabbitListener(queues = "${invoice.queue.payment}")
    @Transactional
    public void handlePaymentEvent(PaymentEvent event) {
        log.info("Received payment event: {}", event);
        
        try {
            switch (event.getEventType()) {
                case "PAYMENT_COMPLETED":
                    updateInvoiceStatusOnPaymentSuccess(event);
                    break;
                case "PAYMENT_FAILED":
                    updateInvoiceStatusOnPaymentFailure(event);
                    break;
                default:
                    log.warn("Unhandled payment event type: {}", event.getEventType());
            }
        } catch (Exception e) {
            log.error("Error processing payment event: {}", e.getMessage(), e);
        }
    }
    
    private void updateInvoiceStatusOnPaymentSuccess(PaymentEvent event) {
        if (event.getInvoiceId() == null) {
            log.error("Cannot update invoice - InvoiceId is missing in event: {}", event);
            return;
        }
        
        try {
            log.info("Updating invoice {} status to PAID", event.getInvoiceId());
            invoiceService.updateInvoiceStatus(event.getInvoiceId(), "PAID", event.getTransactionId());
            log.info("Successfully updated invoice status to PAID");
        } catch (Exception e) {
            log.error("Failed to update invoice status: {}", e.getMessage(), e);
        }
    }
    
    private void updateInvoiceStatusOnPaymentFailure(PaymentEvent event) {
        if (event.getInvoiceId() == null) {
            log.error("Cannot update invoice - InvoiceId is missing in event: {}", event);
            return;
        }
        
        try {
            log.info("Updating invoice {} status to PAYMENT_FAILED", event.getInvoiceId());
            invoiceService.updateInvoiceStatus(event.getInvoiceId(), "PAYMENT_FAILED", event.getTransactionId());
            log.info("Successfully updated invoice status to PAYMENT_FAILED");
        } catch (Exception e) {
            log.error("Failed to update invoice status: {}", e.getMessage(), e);
        }
    }
} 