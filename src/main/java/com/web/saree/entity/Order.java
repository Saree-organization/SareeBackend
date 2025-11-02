package com.web.saree.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String razorpayOrderId;
    private String razorpayPaymentId;
    private String razorpaySignature;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonBackReference // Breaks the circular reference with Users
    private Users user;

    private Double totalAmount;
    private String paymentMethod; //"Online","COD"
    private String paymentStatus; // "Pending", "Success", "Failed"
    private String orderStatus;// "Shipping, "OutOf Delivery, Delivered, Exchange,Exchanged"

    private LocalDateTime createdAt;
    private Long shippingAddressId;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference // Establishes the parent-child relationship with OrderItem
    private List<OrderItem> items;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}