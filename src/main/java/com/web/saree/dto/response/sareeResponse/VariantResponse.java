package com.web.saree.dto.response.sareeResponse;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Data
@Setter
@Getter
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
}
