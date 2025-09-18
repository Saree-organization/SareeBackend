// File: com/web/saree/repository/WishlistRepository.java

package com.web.saree.repository;

import com.web.saree.entity.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface WishlistRepository extends JpaRepository<Wishlist, Long> {

    // Check if a specific saree is already in a user's wishlist
    Optional<Wishlist> findByUserEmailAndSareeId(String email, Long sareeId);

    // Find all wishlist items for a specific user
    List<Wishlist> findByUserEmail(String email);

    // Check if the item exists before deleting
    Optional<Wishlist> findByUserIdAndSareeId(Long userId, Long sareeId);
}