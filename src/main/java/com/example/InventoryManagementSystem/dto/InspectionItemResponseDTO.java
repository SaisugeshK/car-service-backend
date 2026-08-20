package com.example.InventoryManagementSystem.dto;

import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class InspectionItemResponseDTO {

    private Long inspectionItemId;
    private Long jobCardId;
    private String category;
    private String status;
    private String notes;
    private String recommendation;
    private OffsetDateTime updatedAt;
}
