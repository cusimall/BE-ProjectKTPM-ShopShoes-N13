package com.microservice.cartservice.controller;

import com.microservice.cartservice.dto.CartRequest;
import com.microservice.cartservice.models.Cart;
import com.microservice.cartservice.models.CartDetails;
import com.microservice.cartservice.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;

import jakarta.validation.Valid;
import java.util.HashMap;

@RestController
@RequestMapping("/api/carts")
@RequiredArgsConstructor
@Slf4j
public class CartController {

    private final CartService cartService;
    private final WebClient.Builder webClientBuilder;
    @Value("${api.gateway.url}")
    private String apiGatewayUrl;
    
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Cart> getUserCart(@PathVariable Long userId) {
        return ResponseEntity.ok(cartService.getOrCreateCart(userId));
    }
    
    @PostMapping("/user/{userId}/items")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Cart> addToCart(
            @PathVariable Long userId,
            @Valid @RequestBody CartRequest request,
            @RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(cartService.addToCart(userId, request, getTokenValue(token)));
    }
    
    @GetMapping("/{cartId}/items")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<CartDetails>> getCartDetails(@PathVariable Long cartId) {
        return ResponseEntity.ok(cartService.getCartDetails(cartId));
    }
    
    @DeleteMapping("/{cartId}/items/{productId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> removeFromCart(
            @PathVariable Long cartId,
            @PathVariable Long productId) {
        cartService.removeFromCart(cartId, productId);
        return ResponseEntity.ok().build();
    }
    
    @PatchMapping("/{cartId}/items/{productId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<CartDetails> updateQuantity(
            @PathVariable Long cartId,
            @PathVariable Long productId,
            @RequestParam int quantity,
            @RequestHeader("Authorization") String token) {
        CartDetails detail = cartService.updateQuantity(cartId, productId, quantity, getTokenValue(token));
        return ResponseEntity.ok(detail);
    }
    
    @PostMapping("/{cartId}/checkout")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> checkout(
            @PathVariable Long cartId,
            @RequestHeader("Authorization") String token) {
        try {
            Map<String, Object> result = cartService.checkout(cartId, getTokenValue(token));
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Checkout error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "status", "error",
                    "message", e.getMessage()
                ));
        }
    }
    
    @PostMapping("/invoice/{invoiceId}/payment")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> initiatePayment(
            @PathVariable Long invoiceId,
            @RequestBody Map<String, Object> paymentDetails,
            @RequestHeader("Authorization") String token) {
        try {
            log.info("Initiating payment for invoice ID {}", invoiceId);
            
            // Check if invoice exists and belongs to the user
            try {
                Map<String, Object> invoiceData = webClientBuilder.build()
                        .get()
                        .uri(apiGatewayUrl + "/api/v1/invoices/" + invoiceId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + getTokenValue(token))
                        .retrieve()
                        .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                        .block();
                
                if (invoiceData == null || !invoiceData.containsKey("data")) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(Map.of(
                                "status", "error",
                                "message", "Invoice not found"
                            ));
                }
            } catch (WebClientResponseException e) {
                log.error("Error checking invoice: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
                return ResponseEntity.status(e.getStatusCode())
                        .body(Map.of(
                            "status", "error",
                            "message", "Failed to verify invoice: " + e.getMessage()
                        ));
            }
            
            // Create payment request
            Map<String, Object> paymentRequest = new HashMap<>();
            paymentRequest.put("invoiceId", invoiceId);
            paymentRequest.put("description", "Payment for order #" + invoiceId);
            
            // Add optional parameters if provided
            if (paymentDetails.containsKey("bankCode")) {
                paymentRequest.put("bankCode", paymentDetails.get("bankCode"));
            }
            
            if (paymentDetails.containsKey("language")) {
                paymentRequest.put("language", paymentDetails.get("language"));
            }
            
            if (paymentDetails.containsKey("returnUrl")) {
                paymentRequest.put("returnUrl", paymentDetails.get("returnUrl"));
            }
            
            // Call payment service
            Map<String, Object> paymentResponse = webClientBuilder.build()
                    .post()
                    .uri(apiGatewayUrl + "/api/v1/payments/create")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + getTokenValue(token))
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(paymentRequest)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                    .block();
            
            log.info("Payment initiated successfully for invoice {}", invoiceId);
            return ResponseEntity.ok(paymentResponse);
            
        } catch (WebClientResponseException e) {
            log.error("Payment service error: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            return ResponseEntity.status(e.getStatusCode())
                    .body(Map.of(
                        "status", "error",
                        "message", "Payment initiation failed: " + e.getMessage(),
                        "code", e.getStatusCode().value()
                    ));
        } catch (Exception e) {
            log.error("Payment initiation error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                        "status", "error",
                        "message", "Payment initiation failed: " + e.getMessage()
                    ));
        }
    }
    
    @GetMapping("/invoice/{invoiceId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> getInvoiceDetails(
            @PathVariable Long invoiceId,
            @RequestHeader("Authorization") String token) {
        try {
            log.info("Retrieving invoice details for ID: {}", invoiceId);
            
            // Call invoice-service through API gateway
            Map<String, Object> invoiceDetails = webClientBuilder.build()
                    .get()
                    .uri(apiGatewayUrl + "/api/v1/invoices/" + invoiceId)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + getTokenValue(token))
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                    .block();
            
            return ResponseEntity.ok(invoiceDetails);
        } catch (WebClientResponseException e) {
            log.error("Error from invoice service: {} - {}", 
                    e.getStatusCode(), e.getResponseBodyAsString());
            
            return ResponseEntity.status(e.getStatusCode())
                    .body(Map.of(
                        "status", "error",
                        "message", "Failed to get invoice: " + e.getMessage(),
                        "code", e.getStatusCode().value()
                    ));
        } catch (Exception e) {
            log.error("Error retrieving invoice: {}", e.getMessage(), e);
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                        "status", "error",
                        "message", "Failed to get invoice: " + e.getMessage()
                    ));
        }
    }
    
    @GetMapping("/payment/{invoiceId}/status")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> getPaymentStatus(
            @PathVariable Long invoiceId,
            @RequestHeader("Authorization") String token) {
        try {
            log.info("Retrieving payment status for invoice ID: {}", invoiceId);
            
            // Call payment-service through API gateway
            Map<String, Object> paymentStatus = webClientBuilder.build()
                    .get()
                    .uri(apiGatewayUrl + "/api/v1/payments/status/" + invoiceId)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + getTokenValue(token))
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                    .block();
            
            return ResponseEntity.ok(paymentStatus);
        } catch (WebClientResponseException e) {
            log.error("Error from payment service: {} - {}", 
                    e.getStatusCode(), e.getResponseBodyAsString());
            
            return ResponseEntity.status(e.getStatusCode())
                    .body(Map.of(
                        "status", "error",
                        "message", "Failed to get payment status: " + e.getMessage(),
                        "code", e.getStatusCode().value()
                    ));
        } catch (Exception e) {
            log.error("Error retrieving payment status: {}", e.getMessage(), e);
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                        "status", "error",
                        "message", "Failed to get payment status: " + e.getMessage()
                    ));
        }
    }
    
    @DeleteMapping("/{cartId}/clear")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> clearCart(@PathVariable Long cartId) {
        cartService.clearCart(cartId);
        return ResponseEntity.ok().build();
    }
    
    private String getTokenValue(String authHeader) {
        // Remove "Bearer " prefix if present
        return authHeader.startsWith("Bearer ") ? 
                authHeader.substring(7) : authHeader;
    }
} 