package com.example.InventoryManagementSystem.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class LeaveRequestRequestDTO {

    @NotNull(message = "userId is required")
    private Long userId;

    @NotNull(message = "leaveType is required")
    private String leaveType;

    @NotNull(message = "startDate is required")
    private LocalDate startDate;

    @NotNull(message = "endDate is required")
    private LocalDate endDate;

    private String reason;
    private String status;
}
