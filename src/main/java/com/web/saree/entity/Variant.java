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
    private String skuCode;
    private Double costPrice;
    private Integer stock;

    // Store image/video URLs as JSON or comma-separated strings
    @ElementCollection
    private List<String> images;

    private String videos;

    // Many variants belong to one saree
    @ManyToOne
    @JoinColumn(name = "saree_id")
    private Saree saree;

}
