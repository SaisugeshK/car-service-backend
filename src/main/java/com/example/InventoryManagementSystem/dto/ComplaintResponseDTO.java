package com.example.InventoryManagementSystem.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Data
public class ComplaintResponseDTO {

    private Long complaintId;
    private Long customerId;
    private String customerName;
    private Long vehicleId;
    private String vehicleModel;
    private String registrationNumber;
    private Long jobCardId;
    private String jobCardNumber;

    private String type;
    private String description;
    private String priority;
    private Long assignedToUserId;
    private String assignedToName;
    private String status;
    private String resolution;
    private LocalDate resolutionDate;
    private OffsetDateTime createdAt;
}
