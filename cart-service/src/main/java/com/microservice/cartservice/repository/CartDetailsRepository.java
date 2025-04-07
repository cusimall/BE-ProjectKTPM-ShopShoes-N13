package com.microservice.cartservice.repository;

import com.microservice.cartservice.models.CartDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CartDetailsRepository extends JpaRepository<CartDetails, Long> {
    // Các phương thức tương ứng với việc lưu trữ và truy vấn dữ liệu cho CartDetails
}