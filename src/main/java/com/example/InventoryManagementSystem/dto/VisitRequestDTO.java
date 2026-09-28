package com.example.InventoryManagementSystem.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.OffsetDateTime;

// Identify the visitor by mobile number + registration number (the Visits page), or pass
// customerId/vehicleId directly (from a customer's profile). customerName / vehicleModel /
// vehicleCategory are only used when that customer or vehicle doesn't exist yet and is created.
@Data
public class VisitRequestDTO {
    private Long customerId;
    private Long vehicleId;

    private String phone;
    private String registrationNumber;

    private String customerName;
    private String vehicleModel;
    private String vehicleCategory; // CAR / BIKE

    @NotBlank(message = "Purpose is required")
    private String purpose;

    private String notes;

    private OffsetDateTime visitDateTime; // defaults to now
}
