package com.example.InventoryManagementSystem.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Data
public class CustomerFollowUpResponseDTO {

    private Long followUpId;
    private Long customerId;
    private String customerName;
    private String customerPhone;
    private Long vehicleId;
    private String vehicleModel;
    private String registrationNumber;
    private LocalDate reminderDate;
    private String customerResponse;
    private String status;
    private String notes;
    private OffsetDateTime createdAt;
}
