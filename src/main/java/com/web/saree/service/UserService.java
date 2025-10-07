package com.web.saree.service;


import com.twilio.rest.bulkexports.v1.export.ExportCustomJob;
import com.web.saree.dto.response.OrderItemResponse;
import com.web.saree.dto.response.OrderResponse;
import com.web.saree.dto.response.UserResponse;
import com.web.saree.entity.Order;
import com.web.saree.entity.Users;
import com.web.saree.repository.OrderRepository;
import com.web.saree.repository.UserRepository;
import lombok.Data;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Data
public class UserService {
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;

    public Long findIdByEmail(String email) {
        Long userId = userRepository.findByEmail (email).get ().getId ();
        return userId;

    }
    public Users findByEmail(String email) {
       return userRepository.findByEmail (email).get ();
    }

    public boolean isUserExists(String email) {
        return userRepository.findByEmail (email).isPresent ();
    }

    public ResponseEntity<?> findAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<Users> usersPage = userRepository.findAll(pageable);

        // Convert Users -> UserResponse
        List<UserResponse> responseList = usersPage
                .getContent()
                .stream()
                .map(UserResponse::new)
                .toList();

        // Create response map
        Map<String, Object> response = new HashMap<> ();
        response.put("users", responseList);
        response.put("currentPage", usersPage.getNumber());
        response.put("totalItems", usersPage.getTotalElements());
        response.put("totalPages", usersPage.getTotalPages());

        return ResponseEntity.ok(response);
    }


    public ResponseEntity<?> getOrdersByUserId(Long userId) {

        String gmail = userRepository.findGmailById (userId);
        List<Order> orders = orderRepository.findByEmailWithDetails (gmail);

        List<OrderResponse> orderResponses = orders.stream()
                .map(order -> {
                    OrderResponse orderResponse = new OrderResponse();
                    orderResponse.setRazorpayOrderId(order.getRazorpayOrderId());
                    orderResponse.setTotalAmount(order.getTotalAmount());
                    orderResponse.setPaymentStatus(order.getPaymentStatus());
                    orderResponse.setOrderStatus(order.getOrderStatus ());
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

    }

    public String getUserById(Long userId) {
        Users user = userRepository.findById (userId).get ();
        return user.getEmail ();
    }


    public Users findUser(Long userId) {
        return userRepository.findById (userId).get ();
    }
}



