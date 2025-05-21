package com.microservice.emailsenderservice.controller;

import com.microservice.emailsenderservice.dto.EmailRequest;
import com.microservice.emailsenderservice.exception.EmailSendException;
import com.microservice.emailsenderservice.service.EmailService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/email")
@RequiredArgsConstructor
@Slf4j
public class EmailController {

    private final EmailService emailService;

    @PostMapping("/send")
    public ResponseEntity<Map<String, String>> sendEmail(@Valid @RequestBody EmailRequest request) {
        log.info("Received request to send email to: {}", request.getTo());
        
        try {
            emailService.sendEmail(request.getTo(), request.getSubject(), request.getMessage());
            
            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Email sent successfully");
            
            return ResponseEntity.ok(response);
        } catch (EmailSendException e) {
            log.error("Failed to send email: {}", e.getMessage());
            
            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    @PostMapping("/send-html")
    public ResponseEntity<Map<String, String>> sendHtmlEmail(@Valid @RequestBody EmailRequest request) {
        log.info("Received request to send HTML email to: {}", request.getTo());
        
        try {
            emailService.sendHtmlEmail(request.getTo(), request.getSubject(), request.getMessage());
            
            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "HTML email sent successfully");
            
            return ResponseEntity.ok(response);
        } catch (EmailSendException e) {
            log.error("Failed to send HTML email: {}", e.getMessage());
            
            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "up");
        response.put("service", "emailsender-service");
        return ResponseEntity.ok(response);
    }
} 