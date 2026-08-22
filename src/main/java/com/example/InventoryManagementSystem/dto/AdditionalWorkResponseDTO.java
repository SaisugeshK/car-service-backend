package com.example.InventoryManagementSystem.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Data
public class AdditionalWorkResponseDTO {

    private Long additionalWorkRequestId;
    private Long jobCardId;
    private Long requestedByUserId;
    private String requestedByName;
    private String notes;

    private BigDecimal subtotal;
    private BigDecimal discountAmount;
    private BigDecimal taxAmount;
    private BigDecimal grandTotal;

    private String status;
    private OffsetDateTime requestedAt;
    private OffsetDateTime decidedAt;
    private String decidedBy;

    private List<AdditionalWorkItemResponseDTO> items;
}
