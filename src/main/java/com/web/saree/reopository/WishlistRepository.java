 package com.web.saree.reopository;

import com.web.saree.entity.Users;
import com.web.saree.entity.Variant;
import com.web.saree.entity.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WishlistRepository extends JpaRepository<Wishlist, Long> {
    Optional<Wishlist> findByUserAndVariant(Users user, Variant variant);
}