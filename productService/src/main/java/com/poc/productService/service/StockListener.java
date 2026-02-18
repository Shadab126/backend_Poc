package com.poc.productService.service;

import com.poc.productService.config.RabbitMQConfig;
import com.poc.productService.dto.StockMessage;
import com.poc.productService.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StockListener {

    private final ProductService productService;

    @RabbitListener(queues = RabbitMQConfig.STOCK_QUEUE)
    public void receiveStockUpdate(StockMessage message) {

        productService.decreaseStock(
                message.getProductId(),
                message.getQuantity()
        );
    }
}
