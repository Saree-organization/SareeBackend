package com.web.saree.repository;

import com.web.saree.entity.ExchangeRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ExchangeRequestRepository extends JpaRepository<ExchangeRequest, Long> {
    List<ExchangeRequest> findByUserIdOrderByCreatedAtDesc(Long userId);
    Optional<ExchangeRequest> findByRazorpayOrderId(String razorpayOrderId); // Payment Verification के लिए
}