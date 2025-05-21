package com.microservice.emailsenderservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserEvent implements Serializable {
    
    private String eventId;
    private String eventType;
    private LocalDateTime eventTime;
    
    private Long userId;
    private String username;
    private String email;
    private String fullName;
    
    // For password reset events
    private String resetToken;
    private LocalDateTime tokenExpiry;
} 