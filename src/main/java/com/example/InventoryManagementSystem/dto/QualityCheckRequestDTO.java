package com.example.InventoryManagementSystem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class QualityCheckRequestDTO {

    @NotNull(message = "jobCardId is required")
    private Long jobCardId;

    private String checklistJson;

    @NotBlank(message = "result is required (PASS or FAIL)")
    private String result;

    private String notes;
    private Long checkedBy;
}
