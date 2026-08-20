package com.example.InventoryManagementSystem.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "product_taxes")

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class ProductTax {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long taxId;

    private Long productId;

    private String taxName;

    private Double taxPercentage;

    private LocalDateTime createdAt;
}