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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger logger = LoggerFactory.getLogger(PaymentService.class);

    // ----------------------------------------------------------------------------------
    //                             1. ONLINE PAYMENT LOGIC (Updated to set PaymentMethod)
    // ----------------------------------------------------------------------------------

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

        // 🎯 COD CHANGES 1: Set Payment Method for Online
        newOrder.setPaymentMethod("ONLINE");

        logger.info("Setting Shipping Address ID on newOrder object: {}", shippingAddressId);


        newOrder.setShippingAddressId(shippingAddressId);




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

    // ----------------------------------------------------------------------------------
    //                             2. CASH ON DELIVERY (COD) LOGIC
    // ----------------------------------------------------------------------------------

    /**
     * Creates a COD order, sets paymentStatus to PENDING.
     */
    @Transactional
    public com.web.saree.entity.Order createCodOrder(String email, Double amount, Long shippingAddressId) {

        // 1. Fetch User and Cart items
        Users user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + email));

        List<CartItem> cartItems = cartItemRepository.findByUserEmail(email);

        if (cartItems.isEmpty() || amount <= 0) {
            throw new IllegalArgumentException("Cart is empty or order amount is zero.");
        }

        // 2. Create Order Entity
        com.web.saree.entity.Order order = new com.web.saree.entity.Order();
        order.setUser(user);
        order.setTotalAmount(amount);
        order.setShippingAddressId(shippingAddressId);

        // 🎯 COD CHANGES 2: Set COD status
        order.setPaymentMethod("COD");
        order.setPaymentStatus("PENDING"); // Payment upon delivery
        order.setOrderStatus("NEW"); // Initial order status

        // 3. Map Cart Items to Order Items
        List<OrderItem> orderItems = cartItems.stream()
                .map(cartItem -> {
                    OrderItem orderItem = new OrderItem();
                    orderItem.setOrder(order);
                    orderItem.setVariant(cartItem.getVariant());
                    orderItem.setQuantity(cartItem.getQuantity());
                    orderItem.setPrice(cartItem.getVariant().getPriceAfterDiscount());
                    return orderItem;
                })
                .collect(Collectors.toList());

        order.setItems(orderItems);

        // 4. Save Order
        com.web.saree.entity.Order savedOrder = orderRepository.save(order);

        // 5. Clear Cart
        cartItemRepository.deleteAll(cartItemRepository.findByUserEmail(email));

        return savedOrder;
    }

    // ----------------------------------------------------------------------------------
    // 🎯 NEW ADMIN LOGIC: Mark COD Order Paid and Ship
    // ----------------------------------------------------------------------------------

    /**
     * Marks a COD order as paid and transitions its status to 'Shipping'.
     * This is manually triggered by the admin upon cash remittance confirmation.
     */
    @Transactional
    public void markOrderPaidAndShip(Long orderId) {
        com.web.saree.entity.Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found with ID: " + orderId));

        if (!"COD".equals(order.getPaymentMethod())) {
            throw new IllegalStateException("Order is not a COD order.");
        }

        // Check for NEW or PENDING status before processing
        if (!"PENDING".equals(order.getPaymentStatus()) && !"NEW".equals(order.getOrderStatus())) {
            throw new IllegalStateException("Order payment is already processed or the order is not ready for shipping.");
        }

        // Stock deduction logic (Performed here for COD orders, as stock is reserved, not deducted at checkout)
        for (OrderItem item : order.getItems()) {
            com.web.saree.entity.Variant variant = item.getVariant();
            int orderedQuantity = item.getQuantity();

            if (variant.getStock() < orderedQuantity) {
                // Essential stock validation
                throw new IllegalStateException("Insufficient stock for product: " + variant.getName());
            }

            variant.setStock(variant.getStock() - orderedQuantity);
            variantRepository.save(variant);
        }

        // Update payment and order status
        order.setPaymentStatus("Success"); // Payment is confirmed
        order.setOrderStatus("Shipping");
        orderRepository.save(order);
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
            // Stock deduction logic (Correct for online paid orders)
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

    // ----------------------------------------------------------------------------------
    //                             3. CANCELLATION LOGIC
    // ----------------------------------------------------------------------------------

    /**
     * Order Cancellation Logic updated to handle Internal ID (for COD)
     * or Razorpay ID (for Online) passed as 'orderIdentifier', and includes stock refund.
     */
    @Transactional
    public void updateOrderStatusToCancelled(String orderIdentifier) {
        com.web.saree.entity.Order order = null;

        // 1. Try finding by Internal Order ID (Long ID)
        try {
            Long id = Long.parseLong(orderIdentifier);
            order = orderRepository.findById(id).orElse(null);
        } catch (NumberFormatException e) {
            // If not a Long, proceed to search by Razorpay ID
        }

        // 2. If not found by Internal ID, find by Razorpay ID
        if (order == null) {
            order = orderRepository.findByRazorpayOrderId(orderIdentifier)
                    .orElseThrow(() -> new IllegalArgumentException("Order not found with identifier: " + orderIdentifier));
        }

        // Only cancel if it's not already successful or cancelled
        if (!"Success".equalsIgnoreCase(order.getPaymentStatus()) && !"Cancelled".equalsIgnoreCase(order.getOrderStatus())) {

            // Stock refund logic
            for (OrderItem item : order.getItems()) {
                com.web.saree.entity.Variant variant = item.getVariant();
                // Add stock back to inventory
                variant.setStock(variant.getStock() + item.getQuantity());
                variantRepository.save(variant);
            }

            // Update order status
            order.setPaymentStatus("Cancelled");
            order.setOrderStatus("Cancelled");

            orderRepository.save(order);
        }
    }
}