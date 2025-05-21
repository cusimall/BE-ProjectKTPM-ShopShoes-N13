package com.microservice.paymentservice.config;

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

    @Value("${payment.queue.name}")
    private String paymentQueue;

    @Value("${invoice.queue.name}")
    private String invoiceQueue;
    
    @Value("${email.queue.name}")
    private String emailQueue;

    @Bean
    public Queue paymentQueue() {
        return new Queue(paymentQueue, true);
    }

    @Bean
    public Queue invoiceQueue() {
        return new Queue(invoiceQueue, true);
    }
    
    @Bean
    public Queue emailQueue() {
        return new Queue(emailQueue, true);
    }

    @Bean
    public DirectExchange paymentExchange() {
        return new DirectExchange("payment.exchange");
    }

    @Bean
    public Binding paymentBinding(Queue paymentQueue, DirectExchange paymentExchange) {
        return BindingBuilder.bind(paymentQueue).to(paymentExchange).with("payment.events");
    }
    
    @Bean
    public Binding invoiceBinding(Queue invoiceQueue, DirectExchange paymentExchange) {
        return BindingBuilder.bind(invoiceQueue).to(paymentExchange).with("invoice.payment.events");
    }
    
    @Bean
    public Binding emailBinding(Queue emailQueue, DirectExchange paymentExchange) {
        return BindingBuilder.bind(emailQueue).to(paymentExchange).with("email.payment.events");
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public AmqpTemplate amqpTemplate(ConnectionFactory connectionFactory) {
        final RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }
} 