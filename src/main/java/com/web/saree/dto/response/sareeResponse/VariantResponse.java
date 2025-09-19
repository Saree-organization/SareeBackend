// File: com/web/saree/dto/response/sareeResponse/VariantResponse.java

package com.web.saree.dto.response.sareeResponse;

import com.web.saree.entity.Variant; // Import the Variant entity
import lombok.Data;
import java.util.List;

@Data
public class VariantResponse {
    private Long id;
    private String skuCode;
    private String name;
    private String color;
    private Double salesPrice;
    private Double costPrice;
    private Double discountPercent;
    private Double priceAfterDiscount;
    private Integer stock;
    private List<String> images;
    private String video;

    // Add this new constructor to fix the error
    public VariantResponse(Variant variant) {
        this.id = variant.getId();
        this.skuCode = variant.getSkuCode();
        this.name = variant.getName();
        this.color = variant.getColor();
        this.salesPrice = variant.getSalesPrice();
        this.costPrice = variant.getCostPrice();
        this.discountPercent = variant.getDiscountPercent();
        this.priceAfterDiscount = variant.getPriceAfterDiscount();
        this.stock = variant.getStock();
        this.images = variant.getImages();
    }
}