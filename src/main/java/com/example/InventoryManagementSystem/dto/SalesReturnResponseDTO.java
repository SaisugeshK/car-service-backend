package com.example.InventoryManagementSystem.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@Builder
public class SalesReturnResponseDTO {

    private Long returnId;

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

    private OffsetDateTime returnDate;

    private OffsetDateTime createdAt;
}