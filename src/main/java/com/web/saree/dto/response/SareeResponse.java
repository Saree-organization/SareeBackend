package com.web.saree.dto.response;

import lombok.Data;
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

    private List<VariantResponse> variants;

    @Data
    public static class VariantResponse {
        private Long id;
        private String skuCode;
        private String name;
        private String color;
        private Double salesPrice;
        private Double costPrice;
        private Double discountPercent;
        private Integer stock;
        private List<String> images;
        private String video;
    }
}
