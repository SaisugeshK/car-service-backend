package com.example.InventoryManagementSystem.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class JobCardRequestDTO {

    @NotNull(message = "customerId is required")
    private Long customerId;

    @NotNull(message = "vehicleId is required")
    private Long vehicleId;

    private Long advisorUserId;
    private Long technicianUserId;
    private Long appointmentId;

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
}
