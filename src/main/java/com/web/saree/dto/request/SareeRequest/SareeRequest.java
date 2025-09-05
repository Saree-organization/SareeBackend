package com.web.saree.dto.request.SareeRequest;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class SareeRequest {

    private String fabrics;
    private String design;
    private Double length;
    private String description;
    private String border;
    private String category;
    private Double weight;

    private List<VariantRequest> variants = new ArrayList<> ();


    @Override
    public String toString() {
        return "SareeRequest{" +
                "fabrics='" + fabrics + '\'' +
                ", design='" + design + '\'' +
                ", length=" + length +
                ", description='" + description + '\'' +
                ", border='" + border + '\'' +
                ", category='" + category + '\'' +
                ", weight=" + weight +
                ", variants=" + variants +
                '}';
    }
}
