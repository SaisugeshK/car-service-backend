package com.example.InventoryManagementSystem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class VehicleRequestDTO {

    @NotNull(message = "customerId is required")
    private Long customerId;

    private String make;

    @NotBlank(message = "Vehicle model is required")
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
    private String vehicleCategory;
    private String insuranceCompany;
    private LocalDate insuranceExpiry;
    private LocalDate pucExpiry;
    private String notes;
}
