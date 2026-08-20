package com.example.InventoryManagementSystem.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
public class InvoiceItemResponseDTO {

    private Long invoiceItemId;
    private Integer invoiceId;
    private String itemType;
    private Integer serviceId;
    private Integer productId;
    private String itemName;   // resolved service/product name, for display
    private String description;
    private String barcode;
    private BigDecimal quantity;
    private BigDecimal unitPrice;
    private BigDecimal discount;
    private BigDecimal taxPercentage;
    private BigDecimal taxAmount;
    private BigDecimal totalAmount;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
