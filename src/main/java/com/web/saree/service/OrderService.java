package com.web.saree.service;

import com.web.saree.entity.Order;
import com.web.saree.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    public void updateOrderStatus(String razorpayOrderId, String status) {
        Order order = orderRepository.findByRazorpayOrderId(razorpayOrderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + razorpayOrderId));

        order.setOrderStatus(status);
        orderRepository.save(order); // save updated order
    }
}
