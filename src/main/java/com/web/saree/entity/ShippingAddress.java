package com.web.saree.entity;

import jakarta.persistence.*;
import lombok.Data;


@Data
@Entity
@Table(name = "shipping_addresses")
public class ShippingAddress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    private String fullName;
    private String street;
    private String city;
    private String state;
    private String pincode;
    private String phone;

}