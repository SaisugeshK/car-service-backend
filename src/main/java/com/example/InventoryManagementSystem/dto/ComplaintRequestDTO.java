package com.example.InventoryManagementSystem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ComplaintRequestDTO {

    @NotNull(message = "customerId is required")
    private Long customerId;

    private Long vehicleId;
    private Long jobCardId;
    private String type;

    @NotBlank(message = "description is required")
    private String description;

    private String priority;
    private Long assignedToUserId;
    private String status;
    private String resolution;
    private LocalDate resolutionDate;
}
