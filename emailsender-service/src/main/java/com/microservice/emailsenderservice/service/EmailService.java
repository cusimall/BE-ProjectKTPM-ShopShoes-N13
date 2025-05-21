package com.microservice.emailsenderservice.service;

public interface EmailService {
    void sendEmail(String to, String subject, String message);
    void sendHtmlEmail(String to, String subject, String htmlContent);
} 