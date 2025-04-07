package com.microservice.cartservice.services;

import com.microservice.cartservice.client.ProductClient;
import com.microservice.cartservice.client.UserClient;
import com.microservice.cartservice.dto.ProductDTO;
import com.microservice.cartservice.dto.UserDTO;
import com.microservice.cartservice.models.Cart;
import com.microservice.cartservice.models.CartDetails;
import com.microservice.cartservice.models.Product;
import com.microservice.cartservice.models.User;
import com.microservice.cartservice.repository.CartDetailsRepository;
import com.microservice.cartservice.repository.CartRepository;
import com.microservice.cartservice.repository.ProductRepository;
import com.microservice.cartservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface CartService {
    Cart getOrCreateCart(Long userId, String authToken);
    Cart addProductToCart(Long cartId, Long productId, int quantity, String authToken);
    List<CartDetails> getAllCartDetails(Long cartId);
    void removeProductFromCart(Long cartId, Long productId);
    void increaseCartItemQuantity(Long cartId, Long productId);
    void decreaseCartItemQuantity(Long cartId, Long productId);
    void createInvoiceFromCart(Long cartId, String authToken);
}
