package com.microservice.cartservice.service.impl;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microservice.cartservice.dto.ProductDTO;
import com.microservice.cartservice.exception.InsufficientInventoryException;
import com.microservice.cartservice.exception.ProductNotFoundException;
import com.microservice.cartservice.exception.ServiceCommunicationException;
import com.microservice.cartservice.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final WebClient.Builder webClientBuilder;
    
    @Value("${api.gateway.url}")
    private String apiGatewayUrl;
    
    @Override
    public ProductDTO getProductDetails(Long productId, String token) {
        log.debug("Fetching product details for ID: {}", productId);
        
        try {
            // First try to use the internal endpoint
            WebClient.RequestHeadersSpec<?> internalRequest = webClientBuilder.build()
                    .get()
                    .uri(apiGatewayUrl + "/api/products/internal/" + productId);
                    
            // Add token to internal request if available
            if (token != null && !token.isEmpty()) {
                internalRequest = internalRequest.header(HttpHeaders.AUTHORIZATION, 
                        token.startsWith("Bearer ") ? token : "Bearer " + token);
            }
            
            ProductDTO product = internalRequest.retrieve()
                    .bodyToMono(ProductDTO.class)
                    .onErrorResume(WebClientResponseException.class, error -> {
                        log.warn("Internal product endpoint failed, falling back to public endpoint: {}", 
                                error.getMessage());
                        
                        // Fall back to public endpoint with ResponseObject wrapper
                        WebClient.RequestHeadersSpec<?> request = webClientBuilder.build()
                            .get()
                            .uri(apiGatewayUrl + "/api/products/" + productId);
                                
                        if (token != null && !token.isEmpty()) {
                            request = request.header(HttpHeaders.AUTHORIZATION, 
                                    token.startsWith("Bearer ") ? token : "Bearer " + token);
                        }
                        
                        return request.retrieve()
                            .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                            .map(response -> {
                                if (response != null && response.containsKey("data")) {
                                    ObjectMapper mapper = new ObjectMapper();
                                    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                                    
                                    // Extract product from the ResponseObject structure
                                    Object dataObj = response.get("data");
                                    
                                    // Check if the data is wrapped in an Optional
                                    if (dataObj instanceof Map) {
                                        Map<String, Object> dataMap = (Map<String, Object>) dataObj;
                                        if (dataMap.containsKey("get")) {
                                            dataObj = dataMap.get("get");
                                        }
                                    }
                                    
                                    return mapper.convertValue(dataObj, ProductDTO.class);
                                }
                                throw new ProductNotFoundException("Product data not found in response");
                            });
                    })
                    .block();
            
            if (product == null) {
                throw new ProductNotFoundException(productId);
            }
            
            log.debug("Successfully retrieved product: {}", product.getProductName());
            return product;
        } catch (WebClientResponseException e) {
            if (e.getStatusCode().value() == 404) {
                throw new ProductNotFoundException(productId);
            }
            throw new ServiceCommunicationException("product", e.getStatusCode().value(), e.getMessage());
        } catch (ProductNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error fetching product details: {}", e.getMessage(), e);
            throw new ServiceCommunicationException("product", e.getMessage());
        }
    }
    
    @Override
    public boolean hasInStock(Long productId, int quantity, String token) {
        try {
            ProductDTO product = getProductDetails(productId, token);
            boolean hasStock = product.getQuantity() >= quantity;
            
            if (!hasStock) {
                log.warn("Insufficient stock for product {}: requested={}, available={}", 
                        productId, quantity, product.getQuantity());
            }
            
            return hasStock;
        } catch (ProductNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error checking stock for product {}: {}", productId, e.getMessage(), e);
            throw new ServiceCommunicationException("product", e.getMessage());
        }
    }
    
    @Override
    public void validateInventory(Long productId, int quantity, String token) {
        ProductDTO product = getProductDetails(productId, token);
        
        if (product.getQuantity() < quantity) {
            throw new InsufficientInventoryException(productId, quantity, product.getQuantity());
        }
    }
    
    @Override
    public boolean updateInventory(Long productId, int quantity, String token) {
        try {
            // Use the dedicated inventory update endpoint that product-service provides
            Map<String, Integer> request = new HashMap<>();
            request.put("quantity", quantity);
            
            Map<String, Object> response = webClientBuilder.build()
                    .patch()
                    .uri(apiGatewayUrl + "/api/products/" + productId + "/inventory")
                    .header(HttpHeaders.AUTHORIZATION, 
                            token.startsWith("Bearer ") ? token : "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                    .block();
            
            log.debug("Updated inventory for product {}: quantity = {}", productId, quantity);
            return true;
        } catch (WebClientResponseException e) {
            if (e.getStatusCode().value() == 404) {
                throw new ProductNotFoundException(productId);
            }
            if (e.getStatusCode().value() == 400) {
                log.error("Bad request when updating inventory: {}", e.getResponseBodyAsString());
                throw new InsufficientInventoryException(productId, Math.abs(quantity), 0);
            }
            log.error("Error updating inventory for product {}: {} - {}", 
                    productId, e.getStatusCode(), e.getMessage());
            throw new ServiceCommunicationException("product", e.getStatusCode().value(), e.getMessage());
        } catch (ProductNotFoundException | InsufficientInventoryException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error updating inventory for product {}: {}", productId, e.getMessage());
            throw new ServiceCommunicationException("product", e.getMessage());
        }
    }
}