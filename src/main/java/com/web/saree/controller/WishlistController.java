package com.web.saree.controller;

import com.web.saree.dto.response.WishlistResponse;
import com.web.saree.entity.Users;
import com.web.saree.entity.Wishlist;
import com.web.saree.repository.UserRepository;
import com.web.saree.service.WishlistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/wishlist")
@CrossOrigin(origins = "http://localhost:5173")
public class WishlistController {

    @Autowired
    private WishlistService wishlistService;

    @Autowired
    private UserRepository userRepository;

    private String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }

    @PostMapping("/add")
    public ResponseEntity<?> addToWishlist(@RequestBody Map<String, Long> payload) {
        try {
            Long sareeId = payload.get("sareeId");
            wishlistService.addToWishlist(getCurrentUserEmail(), sareeId);
            return ResponseEntity.ok(Map.of("message", "Saree added to wishlist successfully!"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/remove/{sareeId}")
    public ResponseEntity<?> removeFromWishlist(@PathVariable Long sareeId) {
        try {
            wishlistService.removeFromWishlist(getCurrentUserEmail(), sareeId);
            return ResponseEntity.ok(Map.of("message", "Saree removed from wishlist successfully!"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<List<WishlistResponse>> getWishlist() {
        String userEmail = getCurrentUserEmail();
        List<Wishlist> wishlistItems = wishlistService.getWishlistItems(userEmail);

        List<WishlistResponse> responseList = wishlistItems.stream()
                .map(WishlistResponse::new)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responseList);
    }

    @GetMapping("/check/{sareeId}")
    public ResponseEntity<?> checkWishlistStatus(@PathVariable Long sareeId) {
        boolean isInWishlist = wishlistService.isSareeInWishlist(getCurrentUserEmail(), sareeId);
        return ResponseEntity.ok(Map.of("isInWishlist", isInWishlist));
    }
}