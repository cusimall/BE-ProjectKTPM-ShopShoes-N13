package com.microservice.cartservice.service;

import com.microservice.cartservice.dto.ProductDTO;
import com.microservice.cartservice.exception.InsufficientInventoryException;
import com.microservice.cartservice.exception.ProductNotFoundException;
import com.microservice.cartservice.exception.ServiceCommunicationException;

public interface ProductService {
    
    /**
     * Get product details by ID
     * 
     * @param productId Product ID
     * @param token Authentication token
     * @return Product details
     * @throws ProductNotFoundException if product is not found
     * @throws ServiceCommunicationException if there's an error communicating with the product service
     */
    ProductDTO getProductDetails(Long productId, String token);
    
    /**
     * Check if a product has sufficient inventory
     * 
     * @param productId Product ID
     * @param quantity Quantity to check
     * @param token Authentication token
     * @return true if the product has sufficient inventory, false otherwise
     * @throws ProductNotFoundException if product is not found
     * @throws ServiceCommunicationException if there's an error communicating with the product service
     */
    boolean hasInStock(Long productId, int quantity, String token);
    
    /**
     * Check if a product has sufficient inventory and throw an exception if not
     * 
     * @param productId Product ID
     * @param quantity Quantity to check
     * @param token Authentication token
     * @throws InsufficientInventoryException if the product doesn't have sufficient inventory
     * @throws ProductNotFoundException if product is not found
     * @throws ServiceCommunicationException if there's an error communicating with the product service
     */
    void validateInventory(Long productId, int quantity, String token);
    
    /**
     * Update product inventory using the dedicated inventory update endpoint
     * 
     * @param productId Product ID
     * @param quantity Quantity to update to (absolute value, not a delta)
     * @param token Authentication token
     * @return true if successful, false otherwise
     * @throws ProductNotFoundException if product is not found
     * @throws InsufficientInventoryException if the quantity would become negative
     * @throws ServiceCommunicationException if there's an error communicating with the product service
     */
    boolean updateInventory(Long productId, int quantity, String token);
}