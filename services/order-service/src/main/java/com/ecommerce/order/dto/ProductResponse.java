package com.ecommerce.order.dto;

import lombok.Data;

@Data
public class ProductResponse {

    private Long id;
    private String name;
    private String description;
    private double price;
    private int quantity;
}
