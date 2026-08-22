package com.example.InventoryManagementSystem.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "additional_work_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AdditionalWorkItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long additionalWorkItemId;

    @Column(nullable = false)
    private Long additionalWorkRequestId;

    // SERVICE / PRODUCT
    @Column(nullable = false)
    private String itemType;

    private Long serviceId;
    private Long productId;

    private String description;

    private BigDecimal quantity;
    private BigDecimal unitPrice;
    private BigDecimal discount = BigDecimal.ZERO;
    private BigDecimal taxPercentage = BigDecimal.ZERO;
    private BigDecimal taxAmount = BigDecimal.ZERO;
    private BigDecimal totalAmount;
}
