package com.web.saree.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Data
@Table(name = "sarees")
public class Saree {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fabrics;
    private String design;
    private Double weight;
    private String category ;

    private Double length;
    private String description;
    private String border;

    @OneToMany(mappedBy = "saree", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Variant> variants;

}
