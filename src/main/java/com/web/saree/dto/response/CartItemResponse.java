// File: com/web/saree/dto/response/CartItemResponse.java

package com.web.saree.dto.response;

import com.web.saree.entity.CartItem;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CartItemResponse {
    private Long id;
    private VariantDto variant; // Use the new DTO
    private Integer quantity;
    private LocalDateTime createdAt;

    public CartItemResponse(CartItem cartItem) {
        this.id = cartItem.getId();
        this.variant = new VariantDto(cartItem.getVariant()); // Create a new VariantDto
        this.quantity = cartItem.getQuantity();
        this.createdAt = cartItem.getCreatedAt();
    }
}