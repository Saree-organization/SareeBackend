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

    // 🎯 COD CHANGES 1: User के सभी ऑर्डर्स फ़ेच करने के लिए नया मेथड (Tracking Page के लिए)
    /**
     * Retrieves all Orders for a specific user, ordered by creation date descending.
     * Used to show both ONLINE (Success) and COD (Pending) orders.
     */
    Page<Order> findByUserEmailOrderByCreatedAtDesc(String userEmail, Pageable pageable);

    // Finds Orders by User Email, ordered by creation date descending
    List<Order> findByUserEmailOrderByCreatedAtDesc(String userEmail);

    // Custom query to fetch Order, OrderItems, and Variant details for a user
    @Query("SELECT o FROM Order o JOIN FETCH o.items i JOIN FETCH i.variant v WHERE o.user.email = :userEmail ORDER BY o.createdAt DESC")
    List<Order> findByEmailWithDetails(@Param("userEmail") String userEmail);


    // Custom query to fetch Order, OrderItems, and Variant details for a user
    @Query("SELECT o FROM Order o JOIN FETCH o.items i JOIN FETCH i.variant v WHERE o.user.email = :userEmail ORDER BY o.createdAt DESC")
    Page<Order> findByEmailWithDetails(@Param("userEmail") String userEmail, Pageable pageable);

    // ⭐ CORRECTED METHOD: Finds Orders by User's Email (via User object) AND Payment Status
    // This replaces the incorrect 'findByEmailAndPaymentStatus'
    Page<Order> findByUserEmailAndPaymentStatus(String email, String paymentStatus, Pageable pageable);

    Page<Order> findByOrderStatus(String status, Pageable pageable);

    Page<Order> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end, Pageable pageable);

    Page<Order> findByOrderStatusAndCreatedAtBetween(String status, LocalDateTime start, LocalDateTime end, Pageable pageable);


    List<Order> findByOrderStatus(String status);

    List<Order> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    List<Order> findByOrderStatusAndCreatedAtBetween(String status, LocalDateTime start, LocalDateTime end);

}