package com.web.saree.repository;

import com.web.saree.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    // Finds an Order by its Razorpay Order ID
    Optional<Order> findByRazorpayOrderId(String razorpayOrderId);

    // Finds Orders by User Email, ordered by creation date descending (List version)
    List<Order> findByUserEmailOrderByCreatedAtDesc(String userEmail);

    // 🎯 FINAL FIX: Replacing convention-based naming with explicit @Query for Paged fetching.
    // This resolves the runtime error "No property 'paged' found".
    /**
     * Retrieves ALL Orders for a user, ordered by creation date (Paged version).
     */
    @Query("SELECT o FROM Order o JOIN FETCH o.user u WHERE u.email = :userEmail ORDER BY o.createdAt DESC")
    Page<Order> findOrdersByUserEmailPaged(@Param("userEmail") String userEmail, Pageable pageable);

    // Custom query to fetch Order, OrderItems, and Variant details for a user
    @Query("SELECT o FROM Order o JOIN FETCH o.items i JOIN FETCH i.variant v WHERE o.user.email = :userEmail ORDER BY o.createdAt DESC")
    List<Order> findByEmailWithDetails(@Param("userEmail") String userEmail);


    // Custom query to fetch Order, OrderItems, and Variant details for a user (Note: This custom query also needs a JOIN FETCH)
    @Query("SELECT o FROM Order o JOIN FETCH o.items i JOIN FETCH i.variant v WHERE o.user.email = :userEmail ORDER BY o.createdAt DESC")
    Page<Order> findByEmailWithDetails(@Param("userEmail") String userEmail, Pageable pageable);

    // ⭐ CORRECTED METHOD: Finds Orders by User's Email (via User object) AND Payment Status
    Page<Order> findByUserEmailAndPaymentStatus(String email, String paymentStatus, Pageable pageable);

    Page<Order> findByOrderStatus(String status, Pageable pageable);

    Page<Order> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end, Pageable pageable);

    Page<Order> findByOrderStatusAndCreatedAtBetween(String status, LocalDateTime start, LocalDateTime end, Pageable pageable);


    List<Order> findByOrderStatus(String status);

    List<Order> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    List<Order> findByOrderStatusAndCreatedAtBetween(String status, LocalDateTime start, LocalDateTime end);

}