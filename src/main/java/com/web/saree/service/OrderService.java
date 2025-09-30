package com.web.saree.service;

import com.web.saree.dto.response.VariantDto;
import com.web.saree.entity.Order;
import com.web.saree.entity.OrderItem;
import com.web.saree.entity.Variant;
import com.web.saree.repository.OrderItemRepository;
import com.web.saree.repository.OrderRepository;
import com.web.saree.repository.VariantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final VariantRepository variantRepository;

    public void updateOrderStatus(String razorpayOrderId, String status) {
        Order order = orderRepository.findByRazorpayOrderId (razorpayOrderId)
                .orElseThrow (() -> new RuntimeException ("Order not found: " + razorpayOrderId));

        order.setOrderStatus (status);
        orderRepository.save (order); // save updated order
    }

    public List<Variant> getHighestSale() {
        // variant id count is highest
        List<Long>  topVariantIds = orderItemRepository.findTop4VariantIds ();
        for (int i = 0; i < 5; i++) System.out.println ();
        System.out.println ("topVariantIds" + topVariantIds);
        for (int i = 0; i < 5; i++) System.out.println ();
        return variantRepository.findAllById (topVariantIds);
    }
}
