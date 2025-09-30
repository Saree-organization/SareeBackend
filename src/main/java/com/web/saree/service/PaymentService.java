package com.web.saree.service;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import com.web.saree.entity.CartItem;
import com.web.saree.entity.OrderItem;
import com.web.saree.entity.Users;
import com.web.saree.repository.*;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentService {

    @Value("${razorpay.key.id}")
    private String keyId;

    @Value("${razorpay.key.secret}")
    private String keySecret;

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final VariantRepository variantRepository;

    @Transactional
    public Map<String, Object> createRazorpayOrder(String userEmail, Double amount,Long shippingAddressId) throws RazorpayException {
        RazorpayClient razorpayClient = new RazorpayClient(keyId, keySecret);

        // Fetch user and cart items
        Users user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        List<CartItem> cartItems = cartItemRepository.findByUserEmail(userEmail);

        if (cartItems.isEmpty()) {
            throw new IllegalArgumentException("Cart is empty.");
        }

        // 1. Save order to database with 'Created' status (Initial state)
        com.web.saree.entity.Order newOrder = new com.web.saree.entity.Order();
        newOrder.setUser(user);
        newOrder.setTotalAmount(amount);
        newOrder.setPaymentStatus("Created");
        newOrder.setOrderStatus("Created");

        // 💡 NOTE: You should set ShippingAddress ID here if the field exists on Order entity
        // (e.g., newOrder.setShippingAddressId(shippingAddressId);)

        com.web.saree.entity.Order savedOrder = orderRepository.save(newOrder);

        // 2. Save each cart item to OrderItems table
        List<OrderItem> orderItems = cartItems.stream().map(cartItem -> {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(savedOrder);
            orderItem.setVariant(cartItem.getVariant());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPrice(cartItem.getVariant().getPriceAfterDiscount());
            return orderItem;
        }).collect(Collectors.toList());
        orderItemRepository.saveAll(orderItems);

        // 3. Create Razorpay order
        JSONObject orderRequest = new JSONObject();
        orderRequest.put("amount", (int) (amount * 100)); // amount in paisa
        orderRequest.put("currency", "INR");
        orderRequest.put("receipt", "receipt#" + savedOrder.getId());

        Order razorpayOrder = razorpayClient.orders.create(orderRequest);

        // 4. Update database order with Razorpay Order ID
        savedOrder.setRazorpayOrderId(razorpayOrder.get("id"));
        orderRepository.save(savedOrder);

        return Map.of(
                "razorpayOrderId", razorpayOrder.get("id"),
                "amount", razorpayOrder.get("amount"),
                "currency", razorpayOrder.get("currency")
        );
    }

    @Transactional
    public boolean verifyPayment(String razorpayOrderId, String razorpayPaymentId, String razorpaySignature) throws RazorpayException {
        com.web.saree.entity.Order order = orderRepository.findByRazorpayOrderId(razorpayOrderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found with Razorpay Order ID: " + razorpayOrderId));

        // FIX: JSONObject 'options' is correctly defined here
        JSONObject options = new JSONObject();
        options.put("razorpay_order_id", razorpayOrderId);
        options.put("razorpay_payment_id", razorpayPaymentId);
        options.put("razorpay_signature", razorpaySignature);

        boolean isVerified = Utils.verifyPaymentSignature(options, keySecret);

        if (isVerified) {
            // Stock deduction logic
            for (OrderItem item : order.getItems()) {
                com.web.saree.entity.Variant variant = item.getVariant();
                int orderedQuantity = item.getQuantity();

                if (variant.getStock() < orderedQuantity) {
                    throw new IllegalStateException("Insufficient stock for product: " + variant.getName());
                }

                variant.setStock(variant.getStock() - orderedQuantity);
                variantRepository.save(variant);
            }

            order.setPaymentStatus("Success");
            order.setOrderStatus("Shipping");
            order.setRazorpayPaymentId(razorpayPaymentId);
            order.setRazorpaySignature(razorpaySignature);
            orderRepository.save(order);

            // Clear the user's cart
            cartItemRepository.deleteAll(cartItemRepository.findByUserEmail(order.getUser().getEmail()));

            return true;
        } else {
            order.setPaymentStatus("Failed");
            order.setOrderStatus("Failed");
            orderRepository.save(order);
            return false;
        }
    }

    // FIX: Payment Dismissal/Cancellation Logic
    @Transactional
    public void updateOrderStatusToCancelled(String razorpayOrderId) {
        com.web.saree.entity.Order order = orderRepository.findByRazorpayOrderId(razorpayOrderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found with Razorpay ID: " + razorpayOrderId));

        // Only cancel if it's not already successful
        if (!"Success".equalsIgnoreCase(order.getPaymentStatus())) {
            order.setPaymentStatus("Cancelled");
            order.setOrderStatus("Cancelled");

            orderRepository.save(order);
        }
    }
}