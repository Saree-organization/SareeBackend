package com.web.saree.dto.response;

import com.web.saree.dto.response.OrderItemResponse;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderResponse {

    // 🎯 COD FIX 1: Internal Order ID (Long) जोड़ा गया।
    // यह COD ऑर्डर्स के लिए मुख्य पहचानकर्ता (identifier) है।
    private Long id;

    private Long userId;
    private String razorpayOrderId;
    private Double totalAmount;

    // 🎯 COD FIX 2: Payment Method जोड़ा गया।
    // Frontend और Admin पैनल में "COD" या "ONLINE" डिस्प्ले करने के लिए।
    private String paymentMethod;

    private String paymentStatus;
    private String orderStatus;
    private LocalDateTime createdAt;
    private List<OrderItemResponse> items;
}