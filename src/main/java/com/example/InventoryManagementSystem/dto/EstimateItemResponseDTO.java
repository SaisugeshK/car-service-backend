package com.example.InventoryManagementSystem.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class EstimateItemResponseDTO {

    private Long estimateItemId;
    private Long estimateId;
    private String itemType;
    private Long serviceId;
    private Long productId;
    private String itemName;
    private String description;
    private BigDecimal quantity;
    private BigDecimal unitPrice;
    private BigDecimal discount;
    private BigDecimal taxPercentage;
    private BigDecimal taxAmount;
    private BigDecimal totalAmount;
    private String workCategory;
}
