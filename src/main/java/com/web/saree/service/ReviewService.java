package com.web.saree.service;

import com.web.saree.entity.Review;
import com.web.saree.reopository.ReviewRepository;
import lombok.Data;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@Data
public class ReviewService {
    private final ReviewRepository reviewRepository;

    public ResponseEntity<?> getReviews(Long sareeId) {
        return ResponseEntity.ok(reviewRepository.findAllBySareeId(sareeId));
    }

    public ResponseEntity<?> addReview(Review review) {
        reviewRepository.save(review);
        return ResponseEntity.ok("Review added successfully");
    }


}
