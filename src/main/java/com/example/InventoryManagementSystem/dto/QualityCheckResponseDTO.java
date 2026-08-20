package com.example.InventoryManagementSystem.dto;

import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class QualityCheckResponseDTO {

    private Long qualityCheckId;
    private Long jobCardId;
    private String checklistJson;
    private String result;
    private String notes;
    private Long checkedBy;
    private OffsetDateTime checkedAt;
}
