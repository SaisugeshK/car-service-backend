package com.example.InventoryManagementSystem.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class AdditionalWorkRequestDTO {

    @NotNull(message = "jobCardId is required")
    private Long jobCardId;

    private Long requestedByUserId;
    private String notes;

    @NotEmpty(message = "At least one service or product line is required")
    @Valid
    private List<AdditionalWorkLineItemRequestDTO> items;
}
