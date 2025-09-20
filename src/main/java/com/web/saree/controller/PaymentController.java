package com.web.saree.controller;

import com.razorpay.RazorpayException;
import com.web.saree.dto.request.PaymentRequest;
import com.web.saree.dto.request.PaymentVerificationRequest;
import com.web.saree.dto.response.OrderItemResponse;
import com.web.saree.dto.response.OrderResponse;
import com.web.saree.entity.Order;
import com.web.saree.entity.OrderItem;
import com.web.saree.repository.OrderRepository;
import com.web.saree.service.PaymentService;
import com.web.saree.security.CustomUserDetails; // सही क्लास का उपयोग करें
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private  final OrderRepository orderRepository;

    @PostMapping("/create-order")
    public ResponseEntity<?> createOrder(@RequestBody PaymentRequest request, @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            if (userDetails == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated.");
            }
            Map<String, Object> orderDetails = paymentService.createRazorpayOrder(userDetails.getUsername(), request.getAmount());
            return ResponseEntity.ok(orderDetails);
        } catch (RazorpayException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Razorpay Error: " + e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verifyPayment(@RequestBody PaymentVerificationRequest request) {
        try {
            boolean isVerified = paymentService.verifyPayment(
                    request.getRazorpayOrderId(),
                    request.getRazorpayPaymentId(),
                    request.getRazorpaySignature()
            );
            if (isVerified) {
                return ResponseEntity.ok(Map.of("message", "Payment successful and verified!"));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Payment verification failed."));
            }
        } catch (RazorpayException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "Verification Error: " + e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));
        }

    }
    @GetMapping("/orders")
    public ResponseEntity<?> getOrders(@AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            if (userDetails == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated.");
            }

            // यहाँ बदलाव करें: नए मेथड का उपयोग करें
            List<Order> orders = orderRepository.findByEmailWithDetails(userDetails.getUsername());

            List<OrderResponse> orderResponses = orders.stream()
                    .map(order -> {
                        OrderResponse orderResponse = new OrderResponse();
                        orderResponse.setRazorpayOrderId(order.getRazorpayOrderId());
                        orderResponse.setTotalAmount(order.getTotalAmount());
                        orderResponse.setStatus(order.getStatus());
                        orderResponse.setCreatedAt(order.getCreatedAt());

                        List<OrderItemResponse> itemResponses = order.getItems().stream()
                                .map(item -> {
                                    OrderItemResponse itemResponse = new OrderItemResponse();

                                    // ये लाइनें अब काम करेंगी क्योंकि variant डेटा पहले ही लोड हो चुका है
                                    itemResponse.setProductName(item.getVariant().getName());

                                    List<String> images = item.getVariant().getImages();
                                    if (images != null && !images.isEmpty()) {
                                        itemResponse.setImageUrl(images.get(0));
                                    }

                                    itemResponse.setQuantity(item.getQuantity());
                                    itemResponse.setPrice(item.getPrice());
                                    return itemResponse;
                                })
                                .collect(Collectors.toList());
                        orderResponse.setItems(itemResponses);
                        return orderResponse;
                    })
                    .collect(Collectors.toList());

            return ResponseEntity.ok(orderResponses);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "Failed to fetch orders."));
        }
    }

}