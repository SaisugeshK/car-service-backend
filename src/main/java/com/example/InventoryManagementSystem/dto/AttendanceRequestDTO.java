package com.example.InventoryManagementSystem.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class AttendanceRequestDTO {

    @NotNull(message = "userId is required")
    private Long userId;

    @NotNull(message = "attendanceDate is required")
    private LocalDate attendanceDate;

    @NotNull(message = "status is required")
    private String status;

    private String notes;
}
