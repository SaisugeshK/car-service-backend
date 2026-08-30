package com.example.InventoryManagementSystem.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CancelPayrollRequestDTO {

    @NotBlank(message = "cancellationReason is required")
    private String cancellationReason;
}
