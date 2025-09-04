package com.web.saree.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Data
@Table(name = "saree")
public class Saree {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fabrics;
    private String design;
    private Double length;
    private String description;
    private String border;
    private String category;
    private Double weight;

    @OneToMany(mappedBy = "saree", cascade = CascadeType.ALL)
    private List<Variant> variants;
}
