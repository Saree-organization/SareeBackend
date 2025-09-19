// File: com/web/saree/dto/response/CartItemResponse.java

package com.web.saree.dto.response;

import com.web.saree.entity.CartItem;
import com.web.saree.dto.response.sareeResponse.VariantResponse; // VariantResponse को आयात करें
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CartItemResponse {
    private Long id;
    private VariantResponse variant; // VariantResponse का उपयोग करें
    private Integer quantity;
    private LocalDateTime createdAt;

    public CartItemResponse(CartItem cartItem) {
        this.id = cartItem.getId();
        this.variant = new VariantResponse(cartItem.getVariant()); // यहां एक नया VariantResponse बनाएं
        this.quantity = cartItem.getQuantity();
        this.createdAt = cartItem.getCreatedAt();
    }
}