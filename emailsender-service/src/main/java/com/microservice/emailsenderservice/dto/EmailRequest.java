package com.microservice.emailsenderservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmailRequest {

    @NotBlank(message = "Recipient email is required")
    @Email(message = "Please provide a valid email address")
    private String to;
    
    @NotBlank(message = "Subject is required")
    private String subject;
    
    @NotBlank(message = "Message body is required")
    private String message;
} 