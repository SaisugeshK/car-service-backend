package com.example.InventoryManagementSystem.model;


import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "customers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long customerId;

    @Column(nullable = false, length = 100)
    private String customerName;

    private String phone;

    private String email;

    @Column(columnDefinition = "TEXT")
    private String address;

    private String city;

    private String gstin;

    @Column(columnDefinition = "TEXT")
    private String notes;

    private String status = "active";

    private OffsetDateTime createdAt = OffsetDateTime.now();
}