package com.web.saree.reopository;

import com.web.saree.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Integer> {
    List<Review> findAllBySareeId(Long sareeId);
}
