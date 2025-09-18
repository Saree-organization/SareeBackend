
// File: com/web/saree/repository/CartItemRepository.java

package com.web.saree.repository;

import com.web.saree.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    List<CartItem> findByUserEmail(String email);

    Optional<CartItem> findByUserIdAndVariantId(Long userId, Long variantId);

    // Count the number of unique items for a user
    long countByUserEmail(String email);
}