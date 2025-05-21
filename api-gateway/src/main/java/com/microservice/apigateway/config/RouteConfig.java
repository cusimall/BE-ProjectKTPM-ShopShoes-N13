package com.microservice.apigateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RouteConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // Product Service Routes
                .route("product-service", r -> r.path("/api/products/**")
                        .filters(f -> f.circuitBreaker(
                                config -> config.setName("productServiceCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/product-service")))
                        .uri("lb://product-service"))
                
                // Order Service Routes
                .route("order-service", r -> r.path("/api/orders/**")
                        .filters(f -> f.circuitBreaker(
                                config -> config.setName("orderServiceCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/order-service")))
                        .uri("lb://order-service"))
                
                // Authentication Service Routes - Auth endpoints
                .route("auth-service", r -> r.path("/api/auth/**")
                        .filters(f -> f.circuitBreaker(
                                config -> config.setName("authServiceCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/auth-service")))
                        .uri("lb://auth-service"))
                
                // Authentication Service Routes - User management endpoints
                .route("user-management", r -> r.path("/api/users/**")
                        .filters(f -> f.circuitBreaker(
                                config -> config.setName("userManagementCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/auth-service")))
                        .uri("lb://auth-service"))
                
                // Email Service Routes
                .route("emailsender-service", r -> r.path("/api/email/**")
                        .filters(f -> f.circuitBreaker(
                                config -> config.setName("emailServiceCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/email-service")))
                        .uri("lb://emailsender-service"))
                
                // Cart Service Routes
                .route("cart-service", r -> r.path("/api/carts/**")
                        .filters(f -> f.circuitBreaker(
                                config -> config.setName("cartServiceCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/cart-service")))
                        .uri("lb://cart-service"))
                
                // Invoice Service Routes
                .route("invoice-service", r -> r.path("/api/invoices/**")
                        .filters(f -> f.circuitBreaker(
                                config -> config.setName("invoiceServiceCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/invoice-service")))
                        .uri("lb://invoice-service"))
                
                // You can add more service routes as needed
                
                .build();
    }
} 