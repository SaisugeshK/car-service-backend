package com.example.InventoryManagementSystem.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class AppointmentRequestDTO {

    @NotNull(message = "customerId is required")
    private Long customerId;

    private Long vehicleId;
    private String phone;

    @NotNull(message = "appointmentDate is required")
    private LocalDate appointmentDate;

    private LocalTime appointmentTime;
    private String requestedService;
    private String notes;
    private Long advisorUserId;
    private String status;
}
