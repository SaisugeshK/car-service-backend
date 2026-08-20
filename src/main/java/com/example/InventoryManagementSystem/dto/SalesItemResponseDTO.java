package com.example.InventoryManagementSystem.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SalesItemResponseDTO {

    private Long saleItemId;
    private Long saleId;
    private Long productId;
    private String productName;
    private String itemType;

    private Integer quantity;

    private BigDecimal sellingPrice;
    private BigDecimal discount;
    private BigDecimal taxPercentage;
    private BigDecimal taxAmount;
    private BigDecimal total;
}
