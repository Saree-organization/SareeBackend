// File: com/web/saree/controller/CartController.java

package com.web.saree.controller;

import com.web.saree.dto.response.CartItemResponse;
import com.web.saree.entity.CartItem;
import com.web.saree.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/cart")
@CrossOrigin(origins = "http://localhost:5173")
public class CartController {

    @Autowired
    private CartService cartService;

    private String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }

    @PostMapping("/add")
    public ResponseEntity<?> addToCart(@RequestBody Map<String, Object> payload) {
        try {
            Long variantId = Long.valueOf(payload.get("variantId").toString());
            int quantity = Integer.parseInt(payload.get("quantity").toString());
            cartService.addToCart(getCurrentUserEmail(), variantId, quantity);
            return ResponseEntity.ok(Map.of("message", "Item added to cart successfully!"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/remove/{cartItemId}")
    public ResponseEntity<?> removeFromCart(@PathVariable Long cartItemId) {
        try {
            cartService.removeCartItem(getCurrentUserEmail(), cartItemId);
            return ResponseEntity.ok(Map.of("message", "Item removed from cart successfully!"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<List<CartItemResponse>> getCartItems() {
        List<CartItem> cartItems = cartService.getCartItems(getCurrentUserEmail());
        List<CartItemResponse> responseList = cartItems.stream()
                .map(CartItemResponse::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responseList);
    }

    @GetMapping("/count")
    public ResponseEntity<Map<String, Long>> getCartCount() {
        long count = cartService.getCartItemCount(getCurrentUserEmail());
        return ResponseEntity.ok(Map.of("count", count));
    }

    @PutMapping("/update-quantity/{cartItemId}")
    public ResponseEntity<?> updateCartItemQuantity(
            @PathVariable Long cartItemId,
            @RequestBody Map<String, Integer> request) {

        try {
            String userEmail = getCurrentUserEmail();
            Integer newQuantity = request.get("quantity");

            if (newQuantity == null || newQuantity < 1) {
                // Frontend में Quantity 1 से कम नहीं होनी चाहिए, लेकिन यह एक सुरक्षा जाँच है।
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Invalid quantity value."));
            }

            Map<String, Object> updatedItem = cartService.updateQuantity(
                    userEmail,
                    cartItemId,
                    newQuantity
            );

            return ResponseEntity.ok(
                    Map.of("message", "Cart item quantity updated successfully.",
                            "updatedItem", updatedItem)
            );

        } catch (IllegalArgumentException e) {
            // Stock Check या Item Not Found की त्रुटि
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "Failed to update quantity: " + e.getMessage()));
        }
    }
}