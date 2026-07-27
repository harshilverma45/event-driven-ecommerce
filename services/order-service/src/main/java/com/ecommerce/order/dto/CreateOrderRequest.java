package com.ecommerce.order.dto;

import lombok.Data;

@Data
public class CreateOrderRequest {

    private Long productId;
    private Integer quantity;
    private String userEmail;
}
