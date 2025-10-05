package com.web.saree.controller;

import com.razorpay.RazorpayException;
import com.web.saree.dto.request.PaymentRequest;
import com.web.saree.dto.request.PaymentVerificationRequest;
import com.web.saree.dto.response.OrderItemResponse;
import com.web.saree.dto.response.OrderResponse;
import com.web.saree.dto.response.UserResponse;
import com.web.saree.entity.Order;
import com.web.saree.entity.OrderItem;
import com.web.saree.entity.Users;
import com.web.saree.repository.OrderItemRepository;
import com.web.saree.repository.OrderRepository;
import com.web.saree.service.PaymentService;
import com.web.saree.security.CustomUserDetails;
import com.web.saree.service.UserService;
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
    private final OrderRepository orderRepository;
    private final UserService userService;
    private final OrderItemRepository orderItemRepository; // <-- Add this line

    // ******************************************************
    // *** यहाँ बदलाव किया गया है: shippingAddressId को पास करना ***
    // ******************************************************
    @PostMapping("/create-order")
    public ResponseEntity<?> createOrder(@RequestBody PaymentRequest request, @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            if (userDetails == null) {
                return ResponseEntity.status (HttpStatus.UNAUTHORIZED).body ("User not authenticated.");
            }

            // service method को अब shippingAddressId भी pass करना होगा
            Map<String, Object> orderDetails = paymentService.createRazorpayOrder (
                    userDetails.getUsername (),
                    request.getAmount (),
                    request.getShippingAddressId() // PaymentRequest DTO से ID को पास करें
            );

            return ResponseEntity.ok (orderDetails);
        } catch (RazorpayException e) {
            return ResponseEntity.status (HttpStatus.BAD_REQUEST).body (Map.of ("message", "Razorpay Error: " + e.getMessage ()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status (HttpStatus.BAD_REQUEST).body (Map.of ("message", e.getMessage ()));
        }
    }
    // ******************************************************

    @PostMapping("/verify")
    public ResponseEntity<?> verifyPayment(@RequestBody PaymentVerificationRequest request) {
        try {
            boolean isVerified = paymentService.verifyPayment (
                    request.getRazorpayOrderId (),
                    request.getRazorpayPaymentId (),
                    request.getRazorpaySignature ()
            );
            if (isVerified) {
                return ResponseEntity.ok (Map.of ("message", "Payment successful and verified!"));
            } else {
                return ResponseEntity.status (HttpStatus.BAD_REQUEST).body (Map.of ("message", "Payment verification failed."));
            }
        } catch (RazorpayException e) {
            return ResponseEntity.status (HttpStatus.INTERNAL_SERVER_ERROR).body (Map.of ("message", "Verification Error: " + e.getMessage ()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status (HttpStatus.NOT_FOUND).body (Map.of ("message", e.getMessage ()));
        }

    }

    @GetMapping("/orders")
    public ResponseEntity<?> getOrders(@AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            if (userDetails == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated.");
            }

            List<Order> orders = orderRepository.findByUserEmailAndPaymentStatus(
                    userDetails.getUsername(),
                    "Success"
            );

            List<OrderResponse> orderResponses = orders.stream()
                    .map(order -> {
                        OrderResponse orderResponse = new OrderResponse();
                        orderResponse.setRazorpayOrderId(order.getRazorpayOrderId());
                        orderResponse.setTotalAmount(order.getTotalAmount());
                        orderResponse.setPaymentStatus(order.getPaymentStatus());
                        orderResponse.setOrderStatus(order.getOrderStatus());
                        orderResponse.setCreatedAt(order.getCreatedAt());

                        List<OrderItemResponse> itemResponses = order.getItems().stream()
                                .map(item -> {
                                    OrderItemResponse itemResponse = new OrderItemResponse();

                                    // ⭐ FIX: OrderItem ID सेट करें (Exchange के लिए CRITICAL)
                                    itemResponse.setOrderItemId(item.getId());

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
            // ... (Error handling)
            return ResponseEntity.status (HttpStatus.INTERNAL_SERVER_ERROR).body (Map.of ("message", "Failed to fetch orders."));
        }
    }
    // ⭐ NEW API: Exchange Form के लिए एक विशिष्ट Order Item का विवरण प्राप्त करना
    @GetMapping("/order-item/{orderItemId}")
    public ResponseEntity<?> getOrderItemDetails(@PathVariable Long orderItemId, @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            if (userDetails == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated.");
            }

            OrderItem item = orderItemRepository.findById(orderItemId)
                    .orElseThrow(() -> new IllegalArgumentException("Order Item not found."));

            // सुरक्षा जांच: सुनिश्चित करें कि यह आइटम authenticated user का है
            if (!item.getOrder().getUser().getEmail().equals(userDetails.getUsername())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied to this order item.");
            }

            // OrderItemResponse DTO में डेटा मैप करें (इसे सीधे OrderItem Entity न दिखाएं)
            OrderItemResponse itemResponse = new OrderItemResponse();
            itemResponse.setOrderItemId(item.getId());
            itemResponse.setProductName(item.getVariant().getName());
            itemResponse.setQuantity(item.getQuantity());
            itemResponse.setPrice(item.getPrice());

            // वेरिएंट की अन्य जानकारी जो Frontend को चाहिए (जैसे priceAfterDiscount, ID)
            // 💡 Note: यदि Variant की पूरी जानकारी चाहिए, तो आपको एक नया DTO (जैसे VariantExchangeDetailsDTO) बनाना होगा

            return ResponseEntity.ok(itemResponse);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "Failed to fetch order item details."));
        }
    }



    @GetMapping("/admin/user-orders/{userId}")
    public ResponseEntity<?> getUserOrders(@PathVariable("userId") Long userId) {
        try {
            String gmail = userService.getUserById (userId);
            if (gmail == null || gmail.isEmpty ())
                return ResponseEntity.status (HttpStatus.UNAUTHORIZED).body ("User not authenticated.");

            List<Order> orders = orderRepository.findByEmailWithDetails (gmail);

            List<OrderResponse> orderResponses = orders.stream ()
                    .map (order -> {
                        OrderResponse orderResponse = new OrderResponse ();
                        orderResponse.setRazorpayOrderId (order.getRazorpayOrderId ());
                        orderResponse.setTotalAmount (order.getTotalAmount ());
                        orderResponse.setPaymentStatus(order.getPaymentStatus());
                        orderResponse.setOrderStatus(order.getOrderStatus ());
                        orderResponse.setCreatedAt (order.getCreatedAt ());

                        List<OrderItemResponse> itemResponses = order.getItems ().stream ()
                                .map (item -> {
                                    OrderItemResponse itemResponse = new OrderItemResponse ();

                                    // ये लाइनें अब काम करेंगी क्योंकि variant डेटा पहले ही लोड हो चुका है
                                    itemResponse.setProductName (item.getVariant ().getName ());

                                    List<String> images = item.getVariant ().getImages ();
                                    if (images != null && !images.isEmpty ()) {
                                        itemResponse.setImageUrl (images.get (0));
                                    }

                                    itemResponse.setQuantity (item.getQuantity ());
                                    itemResponse.setPrice (item.getPrice ());
                                    return itemResponse;
                                })
                                .collect (Collectors.toList ());
                        orderResponse.setItems (itemResponses);
                        return orderResponse;
                    })
                    .collect (Collectors.toList ());

            return ResponseEntity.ok (orderResponses);
        } catch (Exception e) {
            return ResponseEntity.status (HttpStatus.INTERNAL_SERVER_ERROR).body (Map.of ("message", "Failed to fetch orders."));
        }
    }


    @GetMapping("/admin-orders")
    public ResponseEntity<?> getAllOrdersForAdmin() {
        try {
            List<Order> orders = orderRepository.findAll ();

            List<OrderResponse> orderResponses = orders.stream ()
                    .map (order -> {
                        OrderResponse orderResponse = new OrderResponse ();
                        orderResponse.setUserId (order.getUser ().getId ());
                        orderResponse.setRazorpayOrderId (order.getRazorpayOrderId ());
                        orderResponse.setTotalAmount (order.getTotalAmount ());
                        orderResponse.setPaymentStatus(order.getPaymentStatus());
                        orderResponse.setOrderStatus(order.getOrderStatus ());
                        orderResponse.setCreatedAt (order.getCreatedAt ());

                        List<OrderItemResponse> itemResponses = order.getItems ().stream ()
                                .map (item -> {
                                    OrderItemResponse itemResponse = new OrderItemResponse ();
                                    itemResponse.setProductName (item.getVariant ().getName ());

                                    List<String> images = item.getVariant ().getImages ();
                                    if (images != null && !images.isEmpty ()) {
                                        itemResponse.setImageUrl (images.get (0));
                                    }

                                    itemResponse.setQuantity (item.getQuantity ());
                                    itemResponse.setPrice (item.getPrice ());
                                    return itemResponse;
                                })
                                .collect (Collectors.toList ());
                        orderResponse.setItems (itemResponses);
                        return orderResponse;
                    })
                    .collect (Collectors.toList ());

            return ResponseEntity.ok (orderResponses);
        } catch (Exception e) {
            return ResponseEntity.status (HttpStatus.INTERNAL_SERVER_ERROR)
                    .body (Map.of ("message", "Failed to fetch all orders."));
        }
    }

    @GetMapping("/admin/user/{userId}")
    public ResponseEntity<?> getUser(@PathVariable("userId") Long userId) {
        try {
            String gmail = userService.getUserById(userId);
            if (gmail == null || gmail.isEmpty())
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");

            Users user = userService.findUser(userId);

            // Map Users entity to UserResponse DTO
            UserResponse response = new UserResponse(user);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Failed to fetch user."));
        }
    }
    @PostMapping("/cancel-order")
    public ResponseEntity<?> cancelOrder(@RequestBody Map<String, String> request) {
        try {
            String razorpayOrderId = request.get("razorpayOrderId");

            if (razorpayOrderId == null || razorpayOrderId.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Razorpay Order ID is missing."));
            }


            paymentService.updateOrderStatusToCancelled(razorpayOrderId);

            return ResponseEntity.ok(Map.of("message", "Order " + razorpayOrderId + " successfully marked as Cancelled."));
        } catch (IllegalArgumentException e) {
            // यह तब थ्रो होगा जब orderId नहीं मिलेगा
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "Failed to cancel order: " + e.getMessage()));
        }
    }



}