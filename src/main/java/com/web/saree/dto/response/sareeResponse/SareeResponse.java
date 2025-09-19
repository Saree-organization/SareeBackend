// File: com/web/saree/dto/response/sareeResponse/SareeResponse.java

package com.web.saree.dto.response.sareeResponse;

import com.web.saree.entity.Saree;
import com.web.saree.entity.Variant;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class SareeResponse {

    private Long id;
    private String fabrics;
    private String design;
    private Double length;
    private String description;
    private String border;
    private String category;
    private Double weight;

    private List<VariantResponse> variants = new ArrayList<>();

    public void setSarees(Saree saree) {
        this.id = saree.getId();
        this.fabrics = saree.getFabrics();
        this.design = saree.getDesign();
        this.length = saree.getLength();
        this.description = saree.getDescription();
        this.border = saree.getBorder();
        this.category = saree.getCategory();
        this.weight = saree.getWeight();

        // This is the updated loop that fixes the error
        for (Variant variant : saree.getVariants()) {
            VariantResponse variantResponse = new VariantResponse(variant);
            this.variants.add(variantResponse);
        }
    }
}