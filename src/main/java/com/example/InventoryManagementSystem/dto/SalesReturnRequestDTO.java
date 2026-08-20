package com.example.InventoryManagementSystem.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SalesReturnRequestDTO {

    private Long salesItemId;

    private Long saleId;

    private Long invoiceId;
    private Long invoiceItemId;

    private Long customerId;

    private Integer returnQuantity;

    private String reason;

    private String notes;

    private BigDecimal totalAmount;

    private String refundStatus;
}