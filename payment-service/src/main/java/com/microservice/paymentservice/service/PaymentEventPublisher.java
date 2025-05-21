package com.microservice.paymentservice.service;

import com.microservice.paymentservice.dto.PaymentEvent;
import com.microservice.paymentservice.models.PaymentTransactionEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    /**
     * Publish payment initialized event
     */
    public void publishPaymentInitialized(PaymentTransactionEntity transaction) {
        PaymentEvent event = buildBaseEvent(transaction);
        event.setEventType("PAYMENT_INITIALIZED");
        
        publishToExchange(event);
        log.info("Published payment initialized event for invoice: {}", transaction.getInvoiceId());
    }

    /**
     * Publish payment completed event
     */
    public void publishPaymentCompleted(PaymentTransactionEntity transaction) {
        PaymentEvent event = buildBaseEvent(transaction);
        event.setEventType("PAYMENT_COMPLETED");
        
        publishToExchange(event);
        log.info("Published payment completed event for invoice: {}", transaction.getInvoiceId());
    }

    /**
     * Publish payment failed event
     */
    public void publishPaymentFailed(PaymentTransactionEntity transaction) {
        PaymentEvent event = buildBaseEvent(transaction);
        event.setEventType("PAYMENT_FAILED");
        event.setErrorCode(transaction.getErrorCode());
        event.setErrorMessage(transaction.getErrorMessage());
        
        publishToExchange(event);
        log.info("Published payment failed event for invoice: {}", transaction.getInvoiceId());
    }

    /**
     * Publish to all relevant services via appropriate routing keys
     */
    private void publishToExchange(PaymentEvent event) {
        // Publish to general payment events queue for any service interested in all payment events
        rabbitTemplate.convertAndSend("payment.exchange", "payment.events", event);
        
        // Publish to invoice service specifically 
        rabbitTemplate.convertAndSend("payment.exchange", "invoice.payment.events", event);
        
        // Publish to email service for notifications
        rabbitTemplate.convertAndSend("payment.exchange", "email.payment.events", event);
        
        log.debug("Payment event published: {}", event);
    }

    /**
     * Build base payment event from transaction
     */
    private PaymentEvent buildBaseEvent(PaymentTransactionEntity transaction) {
        return PaymentEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventTime(LocalDateTime.now())
                .transactionId(transaction.getTransactionId())
                .invoiceId(transaction.getInvoiceId())
                .userId(transaction.getUserId())
                .amount(transaction.getAmount())
                .status(transaction.getStatus())
                .paymentMethod(transaction.getPaymentMethod())
                .build();
    }
} 