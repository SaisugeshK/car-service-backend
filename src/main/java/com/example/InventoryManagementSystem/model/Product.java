package com.example.InventoryManagementSystem.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "products")
@Getter
@Setter
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long productId;

    private Long categoryId;

    @Column(nullable = false)
    private String itemType = "PRODUCT"; // PRODUCT / SERVICE (legacy — Catalog now only ever creates PRODUCT; SERVICE rows are frozen leftovers from before service_master existed, kept for old Sales history, never surfaced in Products)

    @Column(nullable = false)
    private String productName;

    private String brand;

    @Column(unique = true)
    private String sku;

    @Column(unique = true)
    private String barcode;

    private BigDecimal purchasePrice;

    private BigDecimal sellingPrice;

    private Integer stockQuantity;

    private Integer minimumStock;

    private String unit;

    // CAR / BIKE / BOTH — null is treated as BOTH (a part usable on either), same convention as
    // ServiceMaster.vehicleType.
    private String vehicleType;

    private String status; // active / inactive

    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt = LocalDateTime.now();

    // Auto update timestamp on update
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}