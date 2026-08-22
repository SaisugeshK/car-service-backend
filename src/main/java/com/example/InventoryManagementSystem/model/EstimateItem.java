package com.example.InventoryManagementSystem.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "estimate_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EstimateItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long estimateItemId;

    @Column(nullable = false)
    private Long estimateId;

    // SERVICE / PRODUCT
    @Column(nullable = false)
    private String itemType;

    private Long serviceId; // set when itemType = SERVICE
    private Long productId; // set when itemType = PRODUCT

    private String description;

    private BigDecimal quantity;
    private BigDecimal unitPrice;
    private BigDecimal discount = BigDecimal.ZERO;
    private BigDecimal taxPercentage = BigDecimal.ZERO;
    private BigDecimal taxAmount = BigDecimal.ZERO;
    private BigDecimal totalAmount;

    // CUSTOMER_REQUESTED (what the customer asked for) / RECOMMENDED (what the technician found
    // during inspection) — kept separate per line so the estimate can show both groups distinctly
    // instead of one flat list. Defaults to RECOMMENDED for anything that doesn't specify it.
    private String workCategory = "RECOMMENDED";
}
