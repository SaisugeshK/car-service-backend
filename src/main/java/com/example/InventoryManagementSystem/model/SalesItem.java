package com.example.InventoryManagementSystem.model;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "sales_items")
@Getter
@Setter
public class SalesItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long saleItemId;

    private Long saleId;

    private Long productId;

    private String itemType; // PRODUCT / SERVICE, snapshotted at sale time

    private Integer quantity;

    private BigDecimal sellingPrice;

    private BigDecimal discount = BigDecimal.ZERO;

    private BigDecimal taxPercentage = BigDecimal.ZERO;

    private BigDecimal taxAmount = BigDecimal.ZERO;

    private BigDecimal total;
}