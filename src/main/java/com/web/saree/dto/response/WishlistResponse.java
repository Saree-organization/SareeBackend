// File: com/web/saree/dto/response/WishlistResponse.java

package com.web.saree.dto.response;

import com.web.saree.dto.response.sareeResponse.AllSareeResponse;
import com.web.saree.dto.response.sareeResponse.SareeResponse;
import com.web.saree.entity.Saree;
import com.web.saree.entity.Wishlist;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class WishlistResponse {

    private Long id;
    private AllSareeResponse saree;
    private LocalDateTime createdAt;

    public WishlistResponse(Wishlist wishlist) {
        this.id = wishlist.getId();
        this.saree = new AllSareeResponse ();
        saree.setSarees(wishlist.getSaree());
        this.createdAt = wishlist.getCreatedAt();
    }

}