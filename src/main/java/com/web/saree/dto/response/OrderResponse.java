package com.web.saree.dto.response;

import com.web.saree.dto.response.OrderItemResponse;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderResponse {
    private Long userId;
    private String razorpayOrderId;
    private Double totalAmount;
    private String paymentStatus;
    private  String orderStatus;
    private LocalDateTime createdAt;
    private List<OrderItemResponse> items;
}