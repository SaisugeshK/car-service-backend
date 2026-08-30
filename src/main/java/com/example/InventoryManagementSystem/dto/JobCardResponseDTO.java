package com.example.InventoryManagementSystem.dto;

import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class JobCardResponseDTO {

    private Long jobCardId;
    private String jobCardNumber;

    private Long customerId;
    private String customerName;
    private String customerPhone;
    private String customerWhatsapp;

    private Long vehicleId;
    private String vehicleModel;
    private String registrationNumber;
    private String vehicleCategory;
    private String vehicleSizeClass;

    private Long advisorUserId;
    private String advisorName;
    private Long technicianUserId;
    private String technicianName;

    private Long appointmentId;
    private Long estimateId;
    private Long invoiceId;
    private String invoiceNumber;

    private OffsetDateTime dateIn;
    private OffsetDateTime expectedDelivery;
    private Integer odometer;
    private String fuelLevel;
    private String complaint;
    private Boolean keysReceived;
    private String accessoriesReceived;
    private String workRequired;
    private String internalNotes;
    private String vehicleConditionNotes;
    private String status;
    private OffsetDateTime deliveredAt;
    private Long deliveredByUserId;
    private String deliveredByName;
    private Boolean vehicleCleaned;
    private Boolean belongingsChecked;
    private Boolean keysReadyForDelivery;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
