package com.web.saree.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "variants")
@Setter
@Getter
public class Variant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String color;

    private Double salesPrice;
    private Double discountPercent;
    private Double priceAfterDiscount;
    private Double costPrice;

    private String skuCode;
    private Integer stock;

    // Store image/video URLs as JSON or comma-separated strings
    @ElementCollection
    private List<String> images;

    private String videos;

    // Many variants belong to one saree
    @ManyToOne
    @JoinColumn(name = "saree_id")
    private Saree saree;

    @PrePersist
    @PreUpdate
    private void roundPrices() {
        if (salesPrice != null) salesPrice = Math.round(salesPrice * 100.0) / 100.0;
        if (costPrice != null) costPrice = Math.round(costPrice * 100.0) / 100.0;
        if (priceAfterDiscount != null) priceAfterDiscount = Math.round(priceAfterDiscount * 100.0) / 100.0;
    }


}
