package com.microservice.cartservice.service;

import com.microservice.cartservice.dto.CartEvent;
import com.microservice.cartservice.dto.CartEventItem;
import com.microservice.cartservice.models.Cart;
import com.microservice.cartservice.models.CartDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@ConditionalOnProperty(name = "spring.rabbitmq.enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
public class CartEventPublisher implements CartEventPublisherInterface {

    private final RabbitTemplate rabbitTemplate;

    @Value("${cart.exchange.name}")
    private String cartExchange;

    @Value("${cart.events.routing-key}")
    private String cartEventsRoutingKey;

    /**
     * Publishes a cart created event
     *
     * @param cart The cart that was created
     * @return true if the event was published successfully, false otherwise
     */
    @Override
    @Retryable(value = {AmqpException.class}, maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2))
    public boolean publishCartCreatedEvent(Cart cart) {
        return publishCartEvent(cart, "CART_CREATED");
    }

    /**
     * Publishes a cart updated event
     *
     * @param cart The cart that was updated
     * @return true if the event was published successfully, false otherwise
     */
    @Override
    @Retryable(value = {AmqpException.class}, maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2))
    public boolean publishCartUpdatedEvent(Cart cart) {
        return publishCartEvent(cart, "CART_UPDATED");
    }

    /**
     * Publishes a cart cleared event
     *
     * @param cart The cart that was cleared
     * @return true if the event was published successfully, false otherwise
     */
    @Override
    @Retryable(value = {AmqpException.class}, maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2))
    public boolean publishCartClearedEvent(Cart cart) {
        return publishCartEvent(cart, "CART_CLEARED");
    }

    /**
     * Publishes a cart checkout event
     *
     * @param cart The cart that was checked out
     * @param invoiceId The ID of the created invoice
     * @return true if the event was published successfully, false otherwise
     */
    @Override
    @Retryable(value = {AmqpException.class}, maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2))
    public boolean publishCartCheckoutEvent(Cart cart, Long invoiceId) {
        try {
            List<CartEventItem> items = mapCartItems(cart.getCartDetails());
            
            CartEvent event = CartEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .eventType("CART_CHECKOUT")
                    .eventTime(LocalDateTime.now())
                    .cartId(cart.getCartId())
                    .userId(cart.getUserId())
                    .total(cart.getTotal())
                    .items(items)
                    .build();
            
            log.info("Publishing cart checkout event: cartId={}, invoiceId={}, userId={}, items={}",
                    cart.getCartId(), invoiceId, cart.getUserId(), items.size());
            
            rabbitTemplate.convertAndSend(cartExchange, cartEventsRoutingKey, event);
            return true;
        } catch (AmqpException ex) {
            log.error("Failed to publish cart checkout event: {}", ex.getMessage(), ex);
            throw ex; // Retryable annotation will handle retry
        } catch (Exception ex) {
            log.error("Unexpected error publishing cart checkout event: {}", ex.getMessage(), ex);
            return false;
        }
    }

    /**
     * Generic method to publish a cart event
     */
    private boolean publishCartEvent(Cart cart, String eventType) {
        if (cart == null) {
            log.warn("Attempted to publish a cart event with null cart - skipping");
            return false;
        }

        try {
            List<CartEventItem> items = mapCartItems(cart.getCartDetails());
            
            CartEvent event = CartEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .eventType(eventType)
                    .eventTime(LocalDateTime.now())
                    .cartId(cart.getCartId())
                    .userId(cart.getUserId())
                    .total(cart.getTotal())
                    .items(items)
                    .build();

            log.info("Publishing cart event: type={}, cartId={}, userId={}, items={}",
                    eventType, cart.getCartId(), cart.getUserId(), items.size());

            rabbitTemplate.convertAndSend(cartExchange, cartEventsRoutingKey, event);
            return true;
        } catch (AmqpException ex) {
            log.error("Failed to publish cart event: {}", ex.getMessage(), ex);
            throw ex; // Retryable annotation will handle retry
        } catch (Exception ex) {
            log.error("Unexpected error publishing cart event: {}", ex.getMessage(), ex);
            return false;
        }
    }

    /**
     * Maps cart details entities to event DTOs
     */
    private List<CartEventItem> mapCartItems(List<CartDetails> cartDetails) {
        if (cartDetails == null) {
            return List.of();
        }
        
        return cartDetails.stream()
                .map(detail -> CartEventItem.builder()
                        .productId(detail.getProductId())
                        .quantity(detail.getQuantity())
                        .price(detail.getTotal().divide(
                                java.math.BigDecimal.valueOf(detail.getQuantity()),
                                java.math.RoundingMode.HALF_UP))
                        .total(detail.getTotal())
                        .build())
                .collect(Collectors.toList());
    }
}