package com.example.InventoryManagementSystem.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SalesReturnItemResponseDTO {

    private Long salesReturnItemId;
    private Long salesReturnId;
    private Long invoiceId;
    private Long invoiceItemId;
    private Long productId;

    private Integer quantity;

    private BigDecimal price;
    private BigDecimal total;
}
