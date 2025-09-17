// File: com/web/saree/dto/response/WishlistResponse.java

package com.web.saree.dto.response;

import com.web.saree.entity.Saree;
import com.web.saree.entity.Wishlist;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class WishlistResponse {

    private Long id;
    private Saree saree;
    private LocalDateTime createdAt;

    public WishlistResponse(Wishlist wishlist) {
        this.id = wishlist.getId();
        this.saree = wishlist.getSaree();
        this.createdAt = wishlist.getCreatedAt();
    }
}