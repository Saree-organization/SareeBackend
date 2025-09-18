package com.web.saree.dto.request.SareeRequest;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
public class VariantRequest {
    private String skuCode;
    private String name;
    private String color;
    private Double salesPrice;
    private Double costPrice;
    private Double priceAfterDiscount;
    private Double discountPercent;
    private Integer stock;

    private List<String> imageUrls;
    private String videoUrl;

}
