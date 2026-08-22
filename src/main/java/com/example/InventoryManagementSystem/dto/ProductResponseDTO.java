package com.example.InventoryManagementSystem.dto;


import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ProductResponseDTO {

    private Long productId;
    private Long categoryId;
    private String itemType;
    private String productName;
    private String brand;
    private String sku;
    private String barcode;
    private BigDecimal purchasePrice;
    private BigDecimal sellingPrice;
    private Integer stockQuantity;
    private Integer minimumStock;
    private String unit;
    private String vehicleType;
    private String status;
    private LocalDateTime createdAt;
}