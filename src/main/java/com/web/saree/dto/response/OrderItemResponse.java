package com.web.saree.dto.response;

import lombok.Data;

@Data
public class OrderItemResponse {
    private String productName;
    private String imageUrl;
    private Integer quantity;
    private Double price;
}