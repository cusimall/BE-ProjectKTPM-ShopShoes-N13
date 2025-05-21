package com.microservice.emailsenderservice.service;

import com.microservice.emailsenderservice.dto.InvoiceEvent;
import com.microservice.emailsenderservice.dto.PaymentEvent;
import com.microservice.emailsenderservice.dto.UserEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class EventListenerService {
    
    private final EmailService emailService;
    
    @RabbitListener(queues = "${email.queue.payment}")
    public void handlePaymentEvent(PaymentEvent event) {
        log.info("Received payment event: {}", event);
        
        try {
            switch (event.getEventType()) {
                case "PAYMENT_INITIALIZED":
                    sendPaymentInitializedEmail(event);
                    break;
                case "PAYMENT_COMPLETED":
                    sendPaymentCompletedEmail(event);
                    break;
                case "PAYMENT_FAILED":
                    sendPaymentFailedEmail(event);
                    break;
                default:
                    log.warn("Unknown payment event type: {}", event.getEventType());
            }
        } catch (Exception e) {
            log.error("Error processing payment event", e);
        }
    }
    
    @RabbitListener(queues = "${email.queue.invoice}")
    public void handleInvoiceEvent(InvoiceEvent event) {
        log.info("Received invoice event: {}", event);
        
        try {
            switch (event.getEventType()) {
                case "INVOICE_CREATED":
                    sendInvoiceCreatedEmail(event);
                    break;
                case "INVOICE_UPDATED":
                    sendInvoiceUpdatedEmail(event);
                    break;
                default:
                    log.warn("Unknown invoice event type: {}", event.getEventType());
            }
        } catch (Exception e) {
            log.error("Error processing invoice event", e);
        }
    }
    
    @RabbitListener(queues = "${email.queue.auth}")
    public void handleUserEvent(UserEvent event) {
        log.info("Received user event: {}", event);
        
        try {
            switch (event.getEventType()) {
                case "USER_REGISTERED":
                    sendWelcomeEmail(event);
                    break;
                case "PASSWORD_RESET_REQUESTED":
                    sendPasswordResetEmail(event);
                    break;
                default:
                    log.warn("Unknown user event type: {}", event.getEventType());
            }
        } catch (Exception e) {
            log.error("Error processing user event", e);
        }
    }
    
    private void sendPaymentInitializedEmail(PaymentEvent event) {
        String subject = "Payment Initiated - Order #" + event.getInvoiceId();
        String body = String.format(
                "Dear %s,\n\n" +
                "We have received your payment request for order #%d.\n" +
                "Amount: %s\n" +
                "Payment Method: %s\n\n" +
                "Your payment is being processed. We will notify you once it's completed.\n\n" +
                "Thank you for shopping with us!\n" +
                "ShopShoes Team",
                event.getCustomerName() != null ? event.getCustomerName() : "Customer",
                event.getInvoiceId(),
                event.getAmount(),
                event.getPaymentMethod()
        );
        
        if (event.getCustomerEmail() != null) {
            emailService.sendEmail(event.getCustomerEmail(), subject, body);
        } else {
            log.warn("Cannot send payment initialized email - customer email is missing");
        }
    }
    
    private void sendPaymentCompletedEmail(PaymentEvent event) {
        String subject = "Payment Confirmed - Order #" + event.getInvoiceId();
        String body = String.format(
                "Dear %s,\n\n" +
                "Your payment for order #%d has been successfully processed.\n" +
                "Amount: %s\n" +
                "Payment Method: %s\n" +
                "Transaction ID: %s\n\n" +
                "Thank you for your purchase!\n" +
                "ShopShoes Team",
                event.getCustomerName() != null ? event.getCustomerName() : "Customer",
                event.getInvoiceId(),
                event.getAmount(),
                event.getPaymentMethod(),
                event.getTransactionId()
        );
        
        if (event.getCustomerEmail() != null) {
            emailService.sendEmail(event.getCustomerEmail(), subject, body);
        } else {
            log.warn("Cannot send payment completed email - customer email is missing");
        }
    }
    
    private void sendPaymentFailedEmail(PaymentEvent event) {
        String subject = "Payment Failed - Order #" + event.getInvoiceId();
        String body = String.format(
                "Dear %s,\n\n" +
                "We're sorry, but your payment for order #%d could not be processed.\n" +
                "Amount: %s\n" +
                "Payment Method: %s\n" +
                "Error: %s\n\n" +
                "Please try again or contact our customer support for assistance.\n\n" +
                "ShopShoes Team",
                event.getCustomerName() != null ? event.getCustomerName() : "Customer",
                event.getInvoiceId(),
                event.getAmount(),
                event.getPaymentMethod(),
                event.getErrorMessage() != null ? event.getErrorMessage() : "Unknown error"
        );
        
        if (event.getCustomerEmail() != null) {
            emailService.sendEmail(event.getCustomerEmail(), subject, body);
        } else {
            log.warn("Cannot send payment failed email - customer email is missing");
        }
    }
    
    private void sendInvoiceCreatedEmail(InvoiceEvent event) {
        String subject = "Your Order #" + event.getInvoiceId() + " has been placed";
        
        StringBuilder itemsText = new StringBuilder();
        if (event.getItems() != null) {
            event.getItems().forEach(item -> 
                itemsText.append(String.format("- %s x%d: $%.2f\n", 
                        item.getProductName(), 
                        item.getQuantity(), 
                        item.getPrice().doubleValue()))
            );
        }
        
        String body = String.format(
                "Dear %s,\n\n" +
                "Thank you for your order!\n\n" +
                "Order #%d\n" +
                "Date: %s\n" +
                "Total Amount: $%.2f\n\n" +
                "Items:\n%s\n" +
                "Please proceed to payment to complete your order.\n\n" +
                "ShopShoes Team",
                event.getCustomerName() != null ? event.getCustomerName() : "Customer",
                event.getInvoiceId(),
                event.getCreatedAt(),
                event.getTotalAmount().doubleValue(),
                itemsText.toString()
        );
        
        if (event.getCustomerEmail() != null) {
            emailService.sendEmail(event.getCustomerEmail(), subject, body);
        } else {
            log.warn("Cannot send invoice created email - customer email is missing");
        }
    }
    
    private void sendInvoiceUpdatedEmail(InvoiceEvent event) {
        String subject = "Order #" + event.getInvoiceId() + " Status Update";
        String body = String.format(
                "Dear %s,\n\n" +
                "Your order #%d has been updated. The current status is: %s\n\n" +
                "Order Details:\n" +
                "Date: %s\n" +
                "Total Amount: $%.2f\n\n" +
                "If you have any questions, please contact our customer support.\n\n" +
                "ShopShoes Team",
                event.getCustomerName() != null ? event.getCustomerName() : "Customer",
                event.getInvoiceId(),
                event.getStatus(),
                event.getCreatedAt(),
                event.getTotalAmount().doubleValue()
        );
        
        if (event.getCustomerEmail() != null) {
            emailService.sendEmail(event.getCustomerEmail(), subject, body);
        } else {
            log.warn("Cannot send invoice updated email - customer email is missing");
        }
    }
    
    private void sendWelcomeEmail(UserEvent event) {
        String subject = "Welcome to ShopShoes!";
        String body = String.format(
                "Dear %s,\n\n" +
                "Welcome to ShopShoes! We're excited to have you as a new customer.\n\n" +
                "Your account has been successfully created. You can now log in and start shopping.\n\n" +
                "Username: %s\n\n" +
                "Thank you for joining us!\n" +
                "ShopShoes Team",
                event.getFullName() != null ? event.getFullName() : event.getUsername(),
                event.getUsername()
        );
        
        if (event.getEmail() != null) {
            emailService.sendEmail(event.getEmail(), subject, body);
        } else {
            log.warn("Cannot send welcome email - user email is missing");
        }
    }
    
    private void sendPasswordResetEmail(UserEvent event) {
        String subject = "Password Reset Request";
        String resetLink = "http://localhost:3000/reset-password?token=" + event.getResetToken();
        
        String body = String.format(
                "Dear %s,\n\n" +
                "We received a request to reset your password. To reset your password, click on the link below:\n\n" +
                "%s\n\n" +
                "This link will expire on %s.\n\n" +
                "If you did not request a password reset, please ignore this email or contact our support team.\n\n" +
                "ShopShoes Team",
                event.getFullName() != null ? event.getFullName() : event.getUsername(),
                resetLink,
                event.getTokenExpiry()
        );
        
        if (event.getEmail() != null) {
            emailService.sendEmail(event.getEmail(), subject, body);
        } else {
            log.warn("Cannot send password reset email - user email is missing");
        }
    }
} 