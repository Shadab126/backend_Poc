package com.poc.productService.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockMessage {

    private Long productId;
    private Integer quantity;
}
