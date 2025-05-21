package com.microservice.cartservice.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "spring.rabbitmq.enabled", havingValue = "true", matchIfMissing = true)
public class RabbitMQConfig {

    @Value("${cart.exchange.name}")
    private String cartExchange;

    @Value("${cart.queue.events}")
    private String cartEventsQueue;

    @Value("${cart.events.routing-key}")
    private String cartEventsRoutingKey;

    @Bean
    public DirectExchange cartExchange() {
        return new DirectExchange(cartExchange);
    }

    @Bean
    public Queue cartEventsQueue() {
        return QueueBuilder.durable(cartEventsQueue)
                .build();
    }

    @Bean
    public Binding cartEventsBinding(Queue cartEventsQueue, DirectExchange cartExchange) {
        return BindingBuilder
                .bind(cartEventsQueue)
                .to(cartExchange)
                .with(cartEventsRoutingKey);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }
}