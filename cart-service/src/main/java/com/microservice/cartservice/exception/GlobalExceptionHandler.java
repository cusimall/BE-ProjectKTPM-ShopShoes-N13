package com.microservice.cartservice.exception;

import com.microservice.cartservice.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(CartNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleCartNotFoundException(CartNotFoundException ex) {
        log.error("Cart not found: {}", ex.getMessage());
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .status("error")
                .message(ex.getMessage())
                .build();
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleProductNotFoundException(ProductNotFoundException ex) {
        log.error("Product not found: {}", ex.getMessage());
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .status("error")
                .message(ex.getMessage())
                .build();
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(InsufficientInventoryException.class)
    public ResponseEntity<ApiResponse<Map<String, Object>>> handleInsufficientInventoryException(InsufficientInventoryException ex) {
        log.error("Insufficient inventory: {}", ex.getMessage());
        
        Map<String, Object> details = new HashMap<>();
        details.put("productId", ex.getProductId());
        details.put("requestedQuantity", ex.getRequestedQuantity());
        details.put("availableQuantity", ex.getAvailableQuantity());
        
        ApiResponse<Map<String, Object>> response = ApiResponse.<Map<String, Object>>builder()
                .status("error")
                .message(ex.getMessage())
                .data(details)
                .build();
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ServiceCommunicationException.class)
    public ResponseEntity<ApiResponse<Void>> handleServiceCommunicationException(ServiceCommunicationException ex) {
        log.error("Service communication error: {}", ex.getMessage());
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .status("error")
                .message(ex.getMessage())
                .build();
        return new ResponseEntity<>(response, HttpStatus.SERVICE_UNAVAILABLE);
    }

    @ExceptionHandler(UnauthorizedAccessException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnauthorizedAccessException(UnauthorizedAccessException ex) {
        log.error("Unauthorized access: {}", ex.getMessage());
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .status("error")
                .message(ex.getMessage())
                .build();
        return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(WebClientResponseException.class)
    public ResponseEntity<ApiResponse<Void>> handleWebClientResponseException(WebClientResponseException ex) {
        log.error("External service error: {} - {}", ex.getStatusCode(), ex.getMessage());
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .status("error")
                .message("Error communicating with external service: " + ex.getMessage())
                .build();
        return new ResponseEntity<>(response, HttpStatus.valueOf(ex.getStatusCode().value()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        log.error("Validation error: {}", ex.getMessage());
        
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        ApiResponse<Map<String, String>> response = ApiResponse.<Map<String, String>>builder()
                .status("error")
                .message("Validation failed")
                .data(errors)
                .build();
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception ex) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .status("error")
                .message("An unexpected error occurred: " + ex.getMessage())
                .build();
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
} 