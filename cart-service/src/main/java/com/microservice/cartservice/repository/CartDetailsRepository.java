package com.microservice.cartservice.repository;

import com.microservice.cartservice.models.CartDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartDetailsRepository extends JpaRepository<CartDetails, Long> {
    Optional<CartDetails> findByCartCartIdAndProductId(Long cartId, Long productId);
    void deleteByCartCartIdAndProductId(Long cartId, Long productId);
}