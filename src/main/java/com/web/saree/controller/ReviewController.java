package com.web.saree.controller;


import com.web.saree.entity.Review;
import com.web.saree.service.ReviewService;
import com.web.saree.service.UserService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/sarees")
@RequiredArgsConstructor
@Data
public class ReviewController {
    private final ReviewService reviewService;
    private final UserService userService;

    @GetMapping("/reviews/{sareeId}")
    public ResponseEntity<?> getReviews(@PathVariable("sareeId") Long sareeId) {
        return reviewService.getReviews(sareeId);
    }

    @PostMapping("/review/add")
    public ResponseEntity<?> addReview(@RequestBody Review review, Principal principal) {
           review.setUserGmail (principal.getName ());
           return  reviewService.addReview(review);
    }
    @GetMapping("/avgRating/{id}")
    public ResponseEntity<?> getRating(@PathVariable("id") Long id) {
        return ResponseEntity.ok(reviewService.getAvgRating(id));
    }
}
