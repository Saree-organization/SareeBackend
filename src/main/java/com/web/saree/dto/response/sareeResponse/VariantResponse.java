package com.web.saree.dto.response.sareeResponse;

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
    private Integer stock;
    private List<String> images;
    private String video;
}
