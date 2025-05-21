package com.microservice.cartservice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    
    @Builder.Default
    private String status = "success";
    
    private String message;
    
    private T data;
    
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
} 