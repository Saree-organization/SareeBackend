package com.web.saree.service;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import com.web.saree.dto.request.CheckoutRequest;
import com.web.saree.dto.response.RazorpayOrderResponse;
import com.web.saree.entity.CartItem;
import com.web.saree.entity.OrderItem;
import com.web.saree.entity.Users;
import com.web.saree.repository.CartItemRepository;
import com.web.saree.repository.OrderRepository;
import com.web.saree.repository.UserRepository;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PaymentService {

    @Value("${razorpay.key.id}")
    private String keyId;

    @Value("${razorpay.key.secret}")
    private String keySecret;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CartItemRepository cartRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CartService cartService;

    public RazorpayOrderResponse createOrder(String userEmail, double totalAmount) throws RazorpayException {
        RazorpayClient razorpay = new RazorpayClient(keyId, keySecret);

        JSONObject orderRequest = new JSONObject();
        orderRequest.put("amount", (int) (totalAmount * 100)); // amount in paisa
        orderRequest.put("currency", "INR");
        orderRequest.put("receipt", "receipt_".concat(String.valueOf(System.currentTimeMillis())));

        Order order = razorpay.orders.create(orderRequest);

        return new RazorpayOrderResponse(order.get("id"), order.get("amount"));
    }

    @Transactional
    public void verifyPaymentAndSaveOrder(String userEmail, CheckoutRequest checkoutRequest) throws RazorpayException {
        Users user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        JSONObject options = new JSONObject();
        options.put("razorpay_order_id", checkoutRequest.getRazorpayOrderId());
        options.put("razorpay_payment_id", checkoutRequest.getRazorpayPaymentId());
        options.put("razorpay_signature", checkoutRequest.getRazorpaySignature());

        boolean isVerified = false;
        try {
            // CORRECTED: Added the keySecret as the second argument
            isVerified = Utils.verifyPaymentSignature(options, keySecret);
        } catch (RazorpayException e) {
            throw new RazorpayException("Payment verification failed: Invalid signature.");
        }

        if (!isVerified) {
            throw new RazorpayException("Payment verification failed: Signature mismatch.");
        }

        // Create new order
        com.web.saree.entity.Order newOrder = new com.web.saree.entity.Order();
        newOrder.setUser(user);
        newOrder.setTotalAmount(checkoutRequest.getTotalAmount());
        newOrder.setRazorpayOrderId(checkoutRequest.getRazorpayOrderId());
        newOrder.setRazorpayPaymentId(checkoutRequest.getRazorpayPaymentId());
        newOrder.setRazorpaySignature(checkoutRequest.getRazorpaySignature());
        newOrder.setPaymentStatus("SUCCESS");

        // Save order items
        List<CartItem> cartItems = cartRepository.findByUserId(user.getId());
        List<OrderItem> orderItems = cartItems.stream().map(cartItem -> {
            OrderItem item = new OrderItem();
            item.setOrder(newOrder);
            item.setSaree(cartItem.getVariant().getSaree());
            item.setVariant(cartItem.getVariant());
            item.setQuantity(cartItem.getQuantity());
            item.setPrice(cartItem.getVariant().getSalesPrice());
            return item;
        }).collect(Collectors.toList());

        newOrder.setOrderItems(new HashSet<>(orderItems));
        orderRepository.save(newOrder);

        // Clear the user's cart
        cartService.clearCart(userEmail);
    }
}