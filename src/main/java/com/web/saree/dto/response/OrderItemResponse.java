package com.web.saree.dto.response;

import lombok.Data;

@Data
public class OrderItemResponse {
    private Long orderItemId; // ⭐ यह फील्ड अब Backend से Frontend में जाएगा

    private String productName;
    private String imageUrl;
    private Integer quantity;
    private Double price;
}