package com.example.InventoryManagementSystem.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

@Data
public class EstimateResponseDTO {

    private Long estimateId;
    private String estimateNumber;
    private Long jobCardId;
    private Long customerId;
    private String customerName;

    private LocalDate validUntil;
    private BigDecimal subtotal;
    private BigDecimal discountAmount;
    private BigDecimal taxAmount;
    private BigDecimal grandTotal;

    private String status;
    private Long rootEstimateId;
    private Integer revisionNumber;
    private OffsetDateTime approvedDate;
    private String approvedBy;
    private String notes;

    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    private List<EstimateItemResponseDTO> items;
}
