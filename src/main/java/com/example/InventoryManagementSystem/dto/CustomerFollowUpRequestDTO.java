package com.example.InventoryManagementSystem.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CustomerFollowUpRequestDTO {

    @NotNull(message = "customerId is required")
    private Long customerId;

    private Long vehicleId;
    private LocalDate reminderDate;
    private String customerResponse;
    private String status;
    private String notes;
}
