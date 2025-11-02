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
import com.web.saree.repository.OrderRepository;
import com.web.saree.service.PaymentService;
import com.web.saree.security.CustomUserDetails;
import com.web.saree.service.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
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

    /**
     * Handles order creation for both COD and Online payments.
     */
    @PostMapping("/create-order")
    public ResponseEntity<?> createOrder(@RequestBody PaymentRequest request, @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            if (userDetails == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated.");
            }

            // 🎯 COD CHANGES 1: Check Payment Method from Frontend
            if ("COD".equalsIgnoreCase(request.getPaymentMethod())) {
                // Case 1: Cash on Delivery (COD)
                Order codOrder = paymentService.createCodOrder(
                        userDetails.getUsername(),
                        request.getAmount(),
                        request.getShippingAddressId()
                );

                // Return Internal ID for client-side tracking
                return ResponseEntity.ok(Map.of(
                        "message", "COD Order placed successfully.",
                        "orderId", codOrder.getId(), // Internal ID
                        "paymentMethod", "COD",
                        "totalAmount", codOrder.getTotalAmount()
                ));

            } else {
                // Case 2: Online Payment (Razorpay)
                Map<String, Object> orderDetails = paymentService.createRazorpayOrder(
                        userDetails.getUsername(),
                        request.getAmount(),
                        request.getShippingAddressId()
                );
                return ResponseEntity.ok(orderDetails);
            }

        } catch (RazorpayException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Razorpay Error: " + e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
        }
    }

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

    /**
     * Retrieves all orders for the authenticated user, regardless of payment status.
     */
    @GetMapping("/orders")
    public ResponseEntity<?> getOrders(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size // default 5 orders per page
    ) {
        try {
            if (userDetails == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("User not authenticated.");
            }

            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

            // 🎯 COD CHANGES 2: Use the method to fetch ALL orders (Online and COD)
            Page<Order> orderPage = orderRepository.findByUserEmailOrderByCreatedAtDesc(
                    userDetails.getUsername(),
                    pageable
            );

            List<OrderResponse> orderResponses = orderPage.getContent().stream()
                    .map(order -> {
                        OrderResponse orderResponse = new OrderResponse();

                        // 🎯 COD CHANGES 3: Map Internal ID and Payment Method
                        orderResponse.setId(order.getId()); // Internal ID
                        orderResponse.setPaymentMethod(order.getPaymentMethod()); // "COD" or "ONLINE"

                        orderResponse.setRazorpayOrderId(order.getRazorpayOrderId());
                        orderResponse.setTotalAmount(order.getTotalAmount());
                        orderResponse.setPaymentStatus(order.getPaymentStatus());
                        orderResponse.setOrderStatus(order.getOrderStatus());
                        orderResponse.setCreatedAt(order.getCreatedAt());

                        List<OrderItemResponse> itemResponses = order.getItems().stream()
                                .map(item -> {
                                    OrderItemResponse itemResponse = new OrderItemResponse();
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

            Map<String, Object> response = new HashMap<>();
            response.put("orders", orderResponses);
            response.put("currentPage", orderPage.getNumber());
            response.put("totalPages", orderPage.getTotalPages());
            response.put("totalItems", orderPage.getTotalElements());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Failed to fetch orders."));
        }
    }

    @GetMapping("/admin/user-orders/{userId}")
    public ResponseEntity<?> getUserOrders(
            @PathVariable("userId") Long userId,
            @RequestParam(defaultValue = "0") int page,   // page index (starts at 0)
            @RequestParam(defaultValue = "10") int size   // page size
    ) {
        try {
            String gmail = userService.getUserById(userId);
            if (gmail == null || gmail.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("User not authenticated.");
            }

            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            Page<Order> ordersPage = orderRepository.findByEmailWithDetails(gmail, pageable);

            List<OrderResponse> orderResponses = ordersPage.getContent().stream()
                    .map(order -> {
                        OrderResponse orderResponse = new OrderResponse();
                        // 🎯 Map Internal ID and Payment Method for admin view as well
                        orderResponse.setId(order.getId());
                        orderResponse.setPaymentMethod(order.getPaymentMethod());

                        orderResponse.setRazorpayOrderId(order.getRazorpayOrderId());
                        orderResponse.setTotalAmount(order.getTotalAmount());
                        orderResponse.setPaymentStatus(order.getPaymentStatus());
                        orderResponse.setOrderStatus(order.getOrderStatus());
                        orderResponse.setCreatedAt(order.getCreatedAt());

                        List<OrderItemResponse> itemResponses = order.getItems().stream()
                                .map(item -> {
                                    OrderItemResponse itemResponse = new OrderItemResponse();
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

            // return both data + pagination info
            Map<String, Object> response = new HashMap<>();
            response.put("orders", orderResponses);
            response.put("currentPage", ordersPage.getNumber());
            response.put("totalItems", ordersPage.getTotalElements());
            response.put("totalPages", ordersPage.getTotalPages());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Failed to fetch orders."));
        }
    }


    @GetMapping("/admin-orders")
    public ResponseEntity<?> getAllOrdersForAdmin(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            Page<Order> orderPage;

            if (status != null && date != null) {
                LocalDateTime startOfDay = date.atStartOfDay();
                LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();
                orderPage = orderRepository.findByOrderStatusAndCreatedAtBetween(status, startOfDay, endOfDay, pageable);
            } else if (status != null) {
                orderPage = orderRepository.findByOrderStatus(status, pageable);
            } else if (date != null) {
                LocalDateTime startOfDay = date.atStartOfDay();
                LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();
                orderPage = orderRepository.findByCreatedAtBetween(startOfDay, endOfDay, pageable);
            } else {
                orderPage = orderRepository.findAll(pageable);
            }

            List<OrderResponse> orderResponses = orderPage.getContent().stream()
                    .map(order -> {
                        OrderResponse orderResponse = new OrderResponse();
                        orderResponse.setUserId(order.getUser().getId());
                        orderResponse.setRazorpayOrderId(order.getRazorpayOrderId());
                        orderResponse.setTotalAmount(order.getTotalAmount());
                        orderResponse.setPaymentStatus(order.getPaymentStatus());
                        orderResponse.setOrderStatus(order.getOrderStatus());
                        orderResponse.setCreatedAt(order.getCreatedAt());

                        // 🎯 Map Internal ID and Payment Method for admin view
                        orderResponse.setId(order.getId());
                        orderResponse.setPaymentMethod(order.getPaymentMethod());

                        List<OrderItemResponse> itemResponses = order.getItems().stream()
                                .map(item -> {
                                    OrderItemResponse itemResponse = new OrderItemResponse();
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

            Map<String, Object> response = new HashMap<>();
            response.put("orders", orderResponses);
            response.put("currentPage", orderPage.getNumber());
            response.put("totalItems", orderPage.getTotalElements());
            response.put("totalPages", orderPage.getTotalPages());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Failed to fetch orders."));
        }
    }

    @GetMapping("/admin-all-orders")
    public ResponseEntity<?> getAllOrders(

            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        try {
            List<Order> orders = orderRepository.findAll();;


            if (status != null && date != null) {
                LocalDateTime startOfDay = date.atStartOfDay();
                LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();
                orders = orderRepository.findByOrderStatusAndCreatedAtBetween(status, startOfDay, endOfDay);
            } else if (status != null) {
                orders = orderRepository.findByOrderStatus(status);
            } else if (date != null) {
                LocalDateTime startOfDay = date.atStartOfDay();
                LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();
                orders = orderRepository.findByCreatedAtBetween(startOfDay, endOfDay);
            } else {
                orders = orderRepository.findAll();
            }


            List<OrderResponse> orderResponses =orders.stream()
                    .map(order -> {
                        OrderResponse orderResponse = new OrderResponse();
                        orderResponse.setUserId(order.getUser().getId());
                        orderResponse.setRazorpayOrderId(order.getRazorpayOrderId());
                        orderResponse.setTotalAmount(order.getTotalAmount());
                        orderResponse.setPaymentStatus(order.getPaymentStatus());
                        orderResponse.setOrderStatus(order.getOrderStatus());
                        orderResponse.setCreatedAt(order.getCreatedAt());

                        // 🎯 Map Internal ID and Payment Method for admin view
                        orderResponse.setId(order.getId());
                        orderResponse.setPaymentMethod(order.getPaymentMethod());

                        List<OrderItemResponse> itemResponses = order.getItems().stream()
                                .map(item -> {
                                    OrderItemResponse itemResponse = new OrderItemResponse();
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

            Map<String, Object> response = new HashMap<>();
            response.put("orders", orderResponses);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Failed to fetch orders."));
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

    /**
     * 🎯 NEW COD ADMIN API: Marks a PENDING COD order as Paid (Success) and sets status to Shipping.
     */
    @PostMapping("/admin/mark-paid-and-ship/{orderId}")
    public ResponseEntity<?> markPaidAndShip(@PathVariable Long orderId) {
        try {
            paymentService.markOrderPaidAndShip(orderId);
            return ResponseEntity.ok(Map.of("message", "Order marked as Paid and Shipped."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "Failed to process COD order: " + e.getMessage()));
        }
    }

    /**
     * Order cancellation, accepting either Internal Order ID (for COD) or Razorpay ID (for Online).
     */
    @PostMapping("/cancel-order")
    public ResponseEntity<?> cancelOrder(@RequestBody Map<String, String> request) {
        try {
            // 🎯 COD CHANGES 4: Now expecting 'orderIdentifier' instead of just 'razorpayOrderId'
            String orderIdentifier = request.get("orderIdentifier"); // Frontend must send this

            if (orderIdentifier == null || orderIdentifier.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Order Identifier (Internal ID or Razorpay ID) is missing."));
            }

            // The service layer will determine the type of ID and perform the cancellation.
            paymentService.updateOrderStatusToCancelled(orderIdentifier);

            return ResponseEntity.ok(Map.of("message", "Order " + orderIdentifier + " successfully marked as Cancelled."));
        } catch (IllegalArgumentException e) {
            // This is thrown when the order is not found by the identifier
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "Failed to cancel order: " + e.getMessage()));
        }
    }
}