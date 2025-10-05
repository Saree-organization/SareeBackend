package com.web.saree.service;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import com.web.saree.entity.*;
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
    private final ExchangeRequestRepository exchangeRepo;

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
    // ⭐ NEW: Exchange Payment Order Creation
    @Transactional
    public Map<String, Object> createExchangePaymentOrder(Long exchangeRequestId, Double amount) throws RazorpayException {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero for exchange payment.");
        }

        ExchangeRequest request = exchangeRepo.findById(exchangeRequestId)
                .orElseThrow(() -> new IllegalArgumentException("Exchange Request not found: " + exchangeRequestId));

        // 1. Razorpay Client
        RazorpayClient razorpayClient = new RazorpayClient(keyId, keySecret);

        // 2. Create Razorpay order for the difference amount
        JSONObject orderRequest = new JSONObject();
        orderRequest.put("amount", (int) (amount * 100));
        orderRequest.put("currency", "INR");
        orderRequest.put("receipt", "exchange_receipt#" + request.getId());

        Order razorpayOrder = razorpayClient.orders.create(orderRequest);

        // 3. Update the ExchangeRequest entity with Razorpay Order ID
        request.setRazorpayOrderId(razorpayOrder.get("id"));
        request.setPaymentStatus("RAZORPAY_CREATED");

        exchangeRepo.save(request);

        return Map.of(
                "razorpayOrderId", razorpayOrder.get("id"),
                "amount", razorpayOrder.get("amount"),
                "currency", razorpayOrder.get("currency")
        );
    }

    // ⭐ NEW: Exchange Payment Verification
    @Transactional
    public boolean verifyExchangePayment(String razorpayOrderId, String razorpayPaymentId, String razorpaySignature) throws RazorpayException {
        ExchangeRequest request = exchangeRepo.findByRazorpayOrderId(razorpayOrderId)
                .orElseThrow(() -> new IllegalArgumentException("Exchange Request not found with Razorpay Order ID: " + razorpayOrderId));

        JSONObject options = new JSONObject();
        options.put("razorpay_order_id", razorpayOrderId);
        options.put("razorpay_payment_id", razorpayPaymentId);
        options.put("razorpay_signature", razorpaySignature);

        boolean isVerified = Utils.verifyPaymentSignature(options, keySecret);

        if (isVerified) {
            request.setPaymentStatus("SUCCESS");
            request.setExchangeStatus("APPROVED_PICKUP_PENDING"); // Payment successful, ready for pickup
            request.setRazorpayPaymentId(razorpayPaymentId);
            request.setRazorpaySignature(razorpaySignature);
            exchangeRepo.save(request);

            // ⭐ Stock Reservation is done here when payment is successful
            // Note: This was handled differently in my previous suggestion, but now it's safer to reserve stock only after payment success.
            Variant newVariant = request.getNewVariant();
            int orderedQuantity = request.getOldOrderItem().getQuantity();
            newVariant.setStock(newVariant.getStock() - orderedQuantity);
            variantRepository.save(newVariant);

            return true;
        } else {
            request.setPaymentStatus("FAILED");
            request.setExchangeStatus("REJECTED_PAYMENT_FAILED");
            exchangeRepo.save(request);
            return false;
        }
    }
}