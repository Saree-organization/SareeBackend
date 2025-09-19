// File: com/web/saree/service/CartService.java

package com.web.saree.service;

import com.web.saree.entity.CartItem;
import com.web.saree.entity.Users;
import com.web.saree.entity.Variant;
import com.web.saree.repository.CartItemRepository;
import com.web.saree.repository.UserRepository;
 import com.web.saree.repository.VariantRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class CartService {

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VariantRepository variantRepository;

    public CartItem addToCart(String userEmail, Long variantId, int quantity) {
        Users user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Variant variant = variantRepository.findById(variantId)
                .orElseThrow(() -> new RuntimeException("Variant not found"));

        Optional<CartItem> existingCartItem = cartItemRepository.findByUserIdAndVariantId(user.getId(), variantId);

        if (existingCartItem.isPresent()) {
            CartItem item = existingCartItem.get();
            item.setQuantity(item.getQuantity() + quantity);
            return cartItemRepository.save(item);
        } else {
            CartItem newCartItem = new CartItem();
            newCartItem.setUser(user);
            newCartItem.setVariant(variant);
            newCartItem.setQuantity(quantity);
            return cartItemRepository.save(newCartItem);
        }
    }

    public List<CartItem> getCartItems(String userEmail) {
        return cartItemRepository.findByUserEmail(userEmail);
    }

    public void updateCartItemQuantity(String userEmail, Long variantId, int newQuantity) {
        Users user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        CartItem item = cartItemRepository.findByUserIdAndVariantId(user.getId(), variantId)
                .orElseThrow(() -> new RuntimeException("Cart item not found"));

        if (newQuantity <= 0) {
            cartItemRepository.delete(item);
        } else {
            item.setQuantity(newQuantity);
            cartItemRepository.save(item);
        }
    }

    public void removeCartItem(String userEmail, Long cartItemId) {
        Users user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Cart item not found"));

        if (!item.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized action");
        }

        cartItemRepository.delete(item);
    }
// File: com/web/saree/service/CartService.java
// ... (existing code)

    @Transactional
    public void clearCart(String userEmail) {
        Users user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<CartItem> cartItems = cartItemRepository.findByUserId(user.getId());
        cartItemRepository.deleteAll(cartItems);
    }
    public long getCartItemCount(String userEmail) {
        return cartItemRepository.countByUserEmail(userEmail);
    }
}