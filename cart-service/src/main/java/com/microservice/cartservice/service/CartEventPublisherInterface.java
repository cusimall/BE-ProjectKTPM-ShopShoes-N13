package com.microservice.cartservice.service;

import com.microservice.cartservice.models.Cart;

/**
 * Interface for publishing cart events to message queue or alternative implementations
 */
public interface CartEventPublisherInterface {
    
    /**
     * Publishes a cart created event
     *
     * @param cart The cart that was created
     * @return true if the event was published successfully, false otherwise
     */
    boolean publishCartCreatedEvent(Cart cart);

    /**
     * Publishes a cart updated event
     *
     * @param cart The cart that was updated
     * @return true if the event was published successfully, false otherwise
     */
    boolean publishCartUpdatedEvent(Cart cart);

    /**
     * Publishes a cart cleared event
     *
     * @param cart The cart that was cleared
     * @return true if the event was published successfully, false otherwise
     */
    boolean publishCartClearedEvent(Cart cart);

    /**
     * Publishes a cart checkout event
     *
     * @param cart The cart that was checked out
     * @param invoiceId The ID of the created invoice
     * @return true if the event was published successfully, false otherwise
     */
    boolean publishCartCheckoutEvent(Cart cart, Long invoiceId);
}