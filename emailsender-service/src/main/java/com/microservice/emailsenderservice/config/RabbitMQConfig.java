package com.microservice.emailsenderservice.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${email.queue.payment}")
    private String paymentEmailQueue;
    
    @Value("${email.queue.invoice}")
    private String invoiceEmailQueue;
    
    @Value("${email.queue.auth}")
    private String authEmailQueue;

    // Define Queues
    @Bean
    public Queue paymentEmailQueue() {
        return new Queue(paymentEmailQueue, true);
    }

    @Bean
    public Queue invoiceEmailQueue() {
        return new Queue(invoiceEmailQueue, true);
    }

    @Bean
    public Queue authEmailQueue() {
        return new Queue(authEmailQueue, true);
    }

    // Message converter for JSON serialization/deserialization
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    // Configure RabbitTemplate
    @Bean
    public AmqpTemplate amqpTemplate(ConnectionFactory connectionFactory) {
        final RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }
} 