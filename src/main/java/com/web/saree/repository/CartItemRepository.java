package com.web.saree.repository;// File: com/web/saree/repository/CartItemRepository.java

import com.web.saree.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    Optional<CartItem> findByUserIdAndVariantId(Long userId, Long variantId);
    List<CartItem> findByUserEmail(String userEmail);
    List<CartItem> findByUserId(Long userId); // Add this line
    long countByUserEmail(String userEmail);
    // Add other methods as needed
}