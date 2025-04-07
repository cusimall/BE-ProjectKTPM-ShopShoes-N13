package com.microservice.cartservice.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "cart")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Cart {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cart_id")
    private Long cartId;

    @Column(name = "user_id")
    private Long userId;
    
    @ManyToOne
    @JoinColumn(name = "user_ref_id")
    private User user;

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "cart", cascade = CascadeType.ALL)
    private List<CartDetails> cartDetails = new ArrayList<>();

    private BigDecimal total = BigDecimal.ZERO;

    public Cart(Long id) {
        this.cartId = id;
    }

    public void updateTotalPrice() {
        BigDecimal totalPrice = BigDecimal.ZERO;
        for (CartDetails cartDetail : cartDetails) {
            totalPrice = totalPrice.add(cartDetail.getProduct().getProductPrice()
                    .multiply(BigDecimal.valueOf(cartDetail.getQuantity())));
        }
        this.total = totalPrice;
    }
}