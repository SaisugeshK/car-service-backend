package com.example.InventoryManagementSystem.dto;

import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class VehicleResponseDTO {

    private Long vehicleId;
    private Long customerId;
    private String customerName;
    private String make;
    private String vehicleModel;
    private String variant;
    private String registrationNumber;
    private Integer odometer;
    private String vehicleType;
    private String fuelType;
    private String color;
    private Integer year;
    private String chassisNumber;
    private String engineNumber;
    private String notes;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
