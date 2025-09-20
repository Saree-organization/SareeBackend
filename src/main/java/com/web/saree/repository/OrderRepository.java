package com.web.saree.repository;

import com.web.saree.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByRazorpayOrderId(String razorpayOrderId);
    List<Order> findByUserEmailOrderByCreatedAtDesc(String userEmail);

    @Query("SELECT o FROM Order o JOIN FETCH o.items i JOIN FETCH i.variant v WHERE o.user.email = :userEmail ORDER BY o.createdAt DESC")
    List<Order> findByEmailWithDetails(@Param("userEmail") String userEmail);


}