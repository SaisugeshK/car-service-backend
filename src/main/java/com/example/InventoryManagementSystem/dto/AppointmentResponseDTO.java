package com.example.InventoryManagementSystem.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;

@Data
public class AppointmentResponseDTO {

    private Long appointmentId;
    private Long customerId;
    private String customerName;
    private Long vehicleId;
    private String vehicleModel;
    private String registrationNumber;
    private String phone;
    private LocalDate appointmentDate;
    private LocalTime appointmentTime;
    private String requestedService;
    private String notes;
    private Long advisorUserId;
    private String status;
    private Long jobCardId;
    private OffsetDateTime createdAt;
}
