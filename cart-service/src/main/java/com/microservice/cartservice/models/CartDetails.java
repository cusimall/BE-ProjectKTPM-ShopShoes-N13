package com.microservice.cartservice.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.microservice.cartservice.dto.ProductDTO;

import java.math.BigDecimal;

@Entity
@Table(name = "CART_DETAILS")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CART_DETAILSID")
    private Long cartDetailsId;
    
    @Column(name = "QUANTITY", nullable = false)
    private Integer quantity;
    
    @Column(name = "TOTAL")
    private BigDecimal total;
    
    @ManyToOne
    @JoinColumn(name = "CARTID")
    @JsonIgnore
    private Cart cart;
    
    @Column(name = "PRODUCT_ID")
    private Long productId;
    
    @Transient
    private ProductDTO product;
}