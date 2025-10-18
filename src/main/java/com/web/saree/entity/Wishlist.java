// File: com/web/saree/entity/Wishlist.java

package com.web.saree.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "wishlists")
@ToString(exclude = {"user"})
public class Wishlist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore // <-- ignore user when serializing Wishlist
    private Users user;


    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "saree_id", nullable = false)
    private Saree saree;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}