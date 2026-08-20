package com.example.InventoryManagementSystem.dto;

import lombok.Data;

import java.math.BigDecimal;

// Used by the standalone Invoice Item CRUD screen (manual line adjustments), distinct from
// InvoiceLineItemRequestDTO which is used by the POS "complete invoice" cascade.
@Data
public class InvoiceItemRequestDTO {

    private Integer invoiceId;
    private String itemType;
    private Integer serviceId;
    private Integer productId;
    private String description;
    private String barcode;
    private BigDecimal quantity;
    private BigDecimal unitPrice;
    private BigDecimal discount;
    private BigDecimal taxPercentage;
}
