// File: com/web/saree/dto/response/VariantDto.java

package com.web.saree.dto.response;

import com.web.saree.entity.Variant;
import lombok.Data;
import java.util.List;

@Data
public class VariantDto {
    private Long id;
    private String name;
    private String color;
    private Double priceAfterDiscount;
    private List<String> images;

    // Saree details needed for display
    private String sareeDesign;
    private String sareeCategory;

    public VariantDto(Variant variant) {
        this.id = variant.getId();
        this.name = variant.getName();
        this.color = variant.getColor();
        this.priceAfterDiscount = variant.getPriceAfterDiscount();
        this.images = variant.getImages();

        if (variant.getSaree() != null) {
            this.sareeDesign = variant.getSaree().getDesign();
            this.sareeCategory = variant.getSaree().getCategory();
        }
    }
}