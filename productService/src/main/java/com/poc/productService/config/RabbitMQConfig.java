package com.poc.productService.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String STOCK_EXCHANGE = "stock.exchange";
    public static final String STOCK_QUEUE = "stock.queue";
    public static final String STOCK_ROUTING_KEY = "stock.update";

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(STOCK_EXCHANGE);
    }

    @Bean
    public Queue queue() {
        return new Queue(STOCK_QUEUE, true);
    }

    @Bean
    public Binding binding(Queue queue, TopicExchange exchange) {
        return BindingBuilder
                .bind(queue)
                .to(exchange)
                .with(STOCK_ROUTING_KEY);
    }
}
