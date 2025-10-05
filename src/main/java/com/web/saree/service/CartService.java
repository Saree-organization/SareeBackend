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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    public long getCartItemCount(String userEmail) {
        return cartItemRepository.countByUserEmail(userEmail);
    }


    @Transactional
    public Map<String, Object> updateQuantity(String userEmail, Long cartItemId, int newQuantity) {

        // 1. कार्ट आइटम खोजें और उपयोगकर्ता की पुष्टि करें
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new IllegalArgumentException("Cart item not found."));

        if (!cartItem.getUser().getEmail().equals(userEmail)) {
            throw new IllegalArgumentException("Unauthorized action: Cart item does not belong to the user.");
        }

        // 2. वेरिएंट (Variant) और स्टॉक (Stock) प्राप्त करें
        Variant variant = cartItem.getVariant();

        // 3. स्टॉक की जाँच करें
        if (newQuantity > variant.getStock()) {
            // यदि मांगी गई क्वांटिटी उपलब्ध स्टॉक से अधिक है, तो त्रुटि दें
            throw new IllegalArgumentException(
                    "Insufficient stock. Only " + variant.getStock() + " units are available for " + variant.getName() + "."
            );
        }

        // 4. क्वांटिटी अपडेट करें
        if (newQuantity <= 0) {
            // यदि क्वांटिटी 0 या उससे कम है, तो आइटम हटा दें (वैकल्पिक, लेकिन अच्छा अभ्यास)
            cartItemRepository.delete(cartItem);
            return new HashMap<>(); // खाली Map वापस करें
        }

        cartItem.setQuantity(newQuantity);
        CartItem updatedItem = cartItemRepository.save(cartItem);

        // 5. अपडेटेड डेटा वापस भेजें
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("id", updatedItem.getId());
        responseMap.put("quantity", updatedItem.getQuantity());
        // Variant डिटेल्स वापस भेजें ताकि Frontend बिना नया API कॉल किए टोटल अपडेट कर सके
        responseMap.put("priceAfterDiscount", updatedItem.getVariant().getPriceAfterDiscount());

        return responseMap;
    }

}