package com.microservice.productservice.controller;

import com.microservice.productservice.dto.PageResponse;
import com.microservice.productservice.dto.ProductRequest;
import com.microservice.productservice.dto.ResponseObject;
import com.microservice.productservice.entity.Product;
import com.microservice.productservice.repository.ProductRepository;
import com.microservice.productservice.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/products")
public class ProductController {
    private final ProductRepository productRepository;
    private final ProductService productService;


    @Autowired
    public ProductController(ProductRepository productRepository, ProductService productService) {
        this.productRepository = productRepository;
        this.productService = productService;

    }

    @GetMapping("/all")
    ResponseEntity<ResponseObject> GetProducts(){
        List<Product> product = productService.getAllProducts();

        if(product != null){
            return ResponseEntity.status(HttpStatus.OK).body(
                    new ResponseObject("ok","get all products successfully", product)
            );
        }
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(
                new ResponseObject("ok", "cannot find product", "")
        );
    }

    @GetMapping("/all-products")
    ResponseEntity<PageResponse> getAllProductPage(@RequestParam Optional<Integer> page){
        Page<Product> pageProducts = productRepository.findAll(PageRequest.of(page.orElse(0), 6));
        return ResponseEntity.status(HttpStatus.OK).body(
                new PageResponse(page, pageProducts.getSize(), pageProducts.getTotalElements(),
                        pageProducts.getTotalPages(),
                        pageProducts.getContent()
                )
        );
    }

    @GetMapping("/shop-products")
    ResponseEntity<PageResponse> getShopProductPage(@RequestParam Optional<Integer> page){
        Page<Product> pageProducts = productRepository.findAll(PageRequest.of(page.orElse(0), 10));
        return ResponseEntity.status(HttpStatus.OK).body(
                new PageResponse(page, pageProducts.getSize(), pageProducts.getTotalElements(),
                        pageProducts.getTotalPages(),
                        pageProducts.getContent()
                )
        );
    }

    @GetMapping("/sort-products")
    ResponseEntity<PageResponse> getShopProductPageSort(@RequestParam Optional<Integer> page,
                                                        @RequestParam Optional<String> sortBy,
                                                        @RequestParam Integer asc
    ){
        if(asc == 1){
            Page<Product> pageProducts = productRepository.findAll(PageRequest.of(page.orElse(0), 10,
                    Sort.Direction.ASC, sortBy.orElse("productPrice")
            ));
            return ResponseEntity.status(HttpStatus.OK).body(
                    new PageResponse(page, pageProducts.getSize(), pageProducts.getTotalElements(),
                            pageProducts.getTotalPages(),
                            pageProducts.getContent()
                    )
            );
        }
        Page<Product> pageProducts = productRepository.findAll(PageRequest.of(page.orElse(0), 10,
                Sort.Direction.DESC, sortBy.orElse("productPrice")
        ));
        return ResponseEntity.status(HttpStatus.OK).body(
                new PageResponse(page, pageProducts.getSize(), pageProducts.getTotalElements(),
                        pageProducts.getTotalPages(),
                        pageProducts.getContent()
                )
        );
    }

    // Add a GET endpoint for add product page
    @GetMapping("/add")
    @PreAuthorize("hasRole('ADMIN')")
    ResponseEntity<ResponseObject> getAddProductPage() {
        return ResponseEntity.status(HttpStatus.OK).body(
                new ResponseObject("ok", "Add product page", "")
        );
    }

    // Add product with ProductRequest
    @PostMapping("/add")
    @PreAuthorize("hasRole('ADMIN')")
    ResponseEntity<ResponseObject> AddProduct(@Valid @RequestBody ProductRequest productRequest, BindingResult bindingResult){
        if (bindingResult.hasErrors()) {
            // Create a custom error message based on validation errors
            StringBuilder errorMessages = new StringBuilder("Validation errors: ");
            bindingResult.getAllErrors().forEach(error -> {
                errorMessages.append(error.getDefaultMessage()).append(" ");
            });

            // Return a simplified error response
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    new ResponseObject("failed", errorMessages.toString().trim(), "")
            );
        }

        Product product = new Product();
        product.setProductName(productRequest.getProductName());
        product.setDescription(productRequest.getDescription());
        product.setCategory(productRequest.getCategory());
        product.setProductPrice(productRequest.getProductPrice());
        product.setImgUrl(productRequest.getImgUrl());
        product.setQuantity(productRequest.getQuantity());
        product.setBrandName(productRequest.getBrandName());
        product.setDesigner(productRequest.getDesigner());

        Product existProduct = productService.addNewProduct(product);
        if (existProduct == null) {
            return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(
                    new ResponseObject("failed", "product name has already been taken", "")
            );
        }
        return ResponseEntity.status(HttpStatus.OK).body(
                new ResponseObject("ok", "product added successfully", existProduct)
        );
    }

    // Get 1 product
    @GetMapping("/{id:[\\d]+}")
    ResponseEntity<ResponseObject> GetDetailProduct(@PathVariable Long id)  {
        Optional<Product> existProduct = productService.getProduct(id);
        return existProduct.isPresent() ?  ResponseEntity.status(HttpStatus.OK).body(
                new ResponseObject("ok", "get success", existProduct)
        ) :   ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                new ResponseObject("failed", "can not find product with "+id, "")
        );
    }

    // Update product with ProductRequest
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    ResponseEntity<ResponseObject> UpdateProduct(@PathVariable Long id, @Valid @RequestBody ProductRequest productRequest){
        Product existProduct = productRepository.findById(id).map((product) -> {
            product.setProductName(productRequest.getProductName());
            product.setDescription(productRequest.getDescription());
            product.setCategory(productRequest.getCategory());
            product.setProductPrice(productRequest.getProductPrice());
            product.setImgUrl(productRequest.getImgUrl());
            product.setQuantity(productRequest.getQuantity());
            product.setBrandName(productRequest.getBrandName());
            product.setDesigner(productRequest.getDesigner());
            return productRepository.save(product);
        }).orElseGet(() -> {
            Product newProduct = new Product();
            newProduct.setProductName(productRequest.getProductName());
            newProduct.setDescription(productRequest.getDescription());
            newProduct.setCategory(productRequest.getCategory());
            newProduct.setProductPrice(productRequest.getProductPrice());
            newProduct.setImgUrl(productRequest.getImgUrl());
            newProduct.setQuantity(productRequest.getQuantity());
            newProduct.setBrandName(productRequest.getBrandName());
            newProduct.setDesigner(productRequest.getDesigner());
            return productRepository.save(newProduct);
        });

        return ResponseEntity.status(HttpStatus.OK).body(
                new ResponseObject("ok", "updated product success", existProduct)
        );
    }

    // Delete 1 product
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    ResponseEntity<ResponseObject> DeleteProduct(@PathVariable Long id){
        boolean existProduct = productRepository.existsById(id);
        if(existProduct){
            productRepository.deleteById(id);
            return ResponseEntity.status(HttpStatus.OK).body(
                    new ResponseObject("ok", "Delete product successfully", "")
            );
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                new ResponseObject("failed", "cannot find product to delete", "")
        );
    }

}
