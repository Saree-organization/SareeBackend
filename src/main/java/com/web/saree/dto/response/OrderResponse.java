package com.web.saree.dto.response;

import com.web.saree.dto.response.OrderItemResponse;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderResponse {

    // 🎯 FIX 1: Internal Order ID (Long) जोड़ा गया।
    // यह COD ऑर्डर्स के लिए मुख्य ID और ट्रैकिंग नंबर के रूप में कार्य करेगा।
    private Long id;

    private Long userId;
    private String razorpayOrderId;
    private Double totalAmount;

    // 🎯 FIX 2: Payment Method जोड़ा गया।
    // यह बताएगा कि ऑर्डर "COD" है या "ONLINE", जिसका उपयोग Frontend डिस्प्ले के लिए करेगा।
    private String paymentMethod;

    private String paymentStatus;
    private  String orderStatus;
    private LocalDateTime createdAt;
    private List<OrderItemResponse> items;
}