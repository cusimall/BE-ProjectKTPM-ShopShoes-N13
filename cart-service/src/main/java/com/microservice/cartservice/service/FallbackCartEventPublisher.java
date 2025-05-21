package com.microservice.cartservice.service;

import com.microservice.cartservice.models.Cart;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

/**
 * Fallback implementation of the cart event publishing logic when RabbitMQ is disabled.
 * This implementation just logs the events rather than publishing them to a message queue.
 */
@Service
@Slf4j
@Primary
@ConditionalOnProperty(name = "spring.rabbitmq.enabled", havingValue = "false")
public class FallbackCartEventPublisher implements CartEventPublisherInterface {

    @Override
    public boolean publishCartCreatedEvent(Cart cart) {
        log.info("RabbitMQ disabled - Logging cart created event: cartId={}, userId={}",
                cart.getCartId(), cart.getUserId());
        return true;
    }

    @Override
    public boolean publishCartUpdatedEvent(Cart cart) {
        log.info("RabbitMQ disabled - Logging cart updated event: cartId={}, userId={}",
                cart.getCartId(), cart.getUserId());
        return true;
    }

    @Override
    public boolean publishCartClearedEvent(Cart cart) {
        log.info("RabbitMQ disabled - Logging cart cleared event: cartId={}, userId={}",
                cart.getCartId(), cart.getUserId());
        return true;
    }

    @Override
    public boolean publishCartCheckoutEvent(Cart cart, Long invoiceId) {
        log.info("RabbitMQ disabled - Logging cart checkout event: cartId={}, invoiceId={}, userId={}",
                cart.getCartId(), invoiceId, cart.getUserId());
        return true;
    }
}