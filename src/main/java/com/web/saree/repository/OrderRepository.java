package com.web.saree.repository;

import com.web.saree.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByRazorpayOrderId(String razorpayOrderId);
    List<Order> findByUserEmailOrderByCreatedAtDesc(String userEmail);
}