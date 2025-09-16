package com.web.saree.dto.request;


import lombok.Data;

@Data // Use Lombok for getters/setters and other boilerplate
public class WishlistRequest {
    private Long sareeId;
    private Long variantId;
}