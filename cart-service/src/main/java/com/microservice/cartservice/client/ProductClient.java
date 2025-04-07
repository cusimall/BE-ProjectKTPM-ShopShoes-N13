package com.microservice.cartservice.client;

import com.microservice.cartservice.dto.ProductDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@Slf4j
public class ProductClient {
    private final WebClient webClient;
    
    @Value("${api.gateway.url}")
    private String apiGatewayUrl;
    
    public ProductClient(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("http://localhost:8081").build();
    }
    
    public ProductDTO getProductById(Long productId, String authToken) {
        return webClient.get()
                .uri("/api/products/{id}", productId)
                .header("Authorization", "Bearer " + authToken)
                .retrieve()
                .bodyToMono(ProductDTO.class)
                .doOnError(error -> log.error("Error fetching product with ID {}: {}", productId, error.getMessage()))
                .block();
    }
} 