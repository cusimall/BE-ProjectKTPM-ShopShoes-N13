package com.microservice.productservice.service;

import com.microservice.productservice.entity.Product;

import java.util.List;
import java.util.Optional;

public interface ProductService {
     List<Product> getAllProducts();
     Product addNewProduct(Product product);
     Optional<Product> getProduct(Long id);
     Product getProductById(Long id);
     boolean existsByProductName(String productName);
}
