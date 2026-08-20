package com.example.InventoryManagementSystem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class InspectionItemRequestDTO {

    @NotNull(message = "jobCardId is required")
    private Long jobCardId;

    @NotBlank(message = "category is required")
    private String category;

    private String status;
    private String notes;
    private String recommendation;
}
