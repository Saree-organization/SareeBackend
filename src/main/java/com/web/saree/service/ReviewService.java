package com.web.saree.service;

import com.web.saree.entity.Review;
import com.web.saree.repository.ReviewRepository;
import lombok.Data;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@Data
public class ReviewService {
    private final ReviewRepository reviewRepository;

    public ResponseEntity<?> getReviews(Long sareeId) {
        return ResponseEntity.ok (reviewRepository.findAllBySareeId (sareeId));
    }

    public ResponseEntity<?> addReview(Review review) {
        reviewRepository.save (review);
        return ResponseEntity.ok ("Review added successfully");
    }


    public ResponseEntity<?> getAvgRating(Long id) {
        List<Review> avgRating = reviewRepository.findAllBySareeId (id);
        Double rating = avgRating.stream ().mapToDouble (Review::getRating).average ().orElse (0.0);
        int count = avgRating.size ();
        return ResponseEntity.ok (Map.of ("rating", rating, "count", count));

    }
}
