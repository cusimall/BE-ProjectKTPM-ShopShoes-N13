package com.microservice.invoiceservice.config;

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

    @Value("${invoice.queue.payment}")
    private String paymentInvoiceQueue;
    
    @Value("${invoice.queue.name}")
    private String invoiceQueue;

    @Bean
    public Queue paymentInvoiceQueue() {
        return new Queue(paymentInvoiceQueue, true);
    }

    @Bean
    public Queue invoiceQueue() {
        return new Queue(invoiceQueue, true);
    }

    @Bean
    public DirectExchange invoiceExchange() {
        return new DirectExchange("invoice.exchange");
    }

    @Bean
    public Binding invoiceBinding(Queue invoiceQueue, DirectExchange invoiceExchange) {
        return BindingBuilder.bind(invoiceQueue).to(invoiceExchange).with("invoice.events");
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