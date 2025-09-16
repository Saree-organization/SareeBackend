package com.web.saree.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "reviews")
@Data
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String comment;
    private Long rating;
    private Long sareeId;
    private String userGmail;
}
