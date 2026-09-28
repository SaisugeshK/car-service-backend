package com.example.InventoryManagementSystem.dto;

import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class VisitResponseDTO {
    private Long visitId;
    private Long customerId;
    private String customerName;
    private String customerPhone;
    private String customerRegularStatus;
    private Integer customerTotalVisits;
    private Long vehicleId;
    private String vehicleModel;
    private String registrationNumber;
    private OffsetDateTime visitDateTime;
    private String purpose;
    private String notes;
    private Long handledByUserId;
    private String handledByName;
    private String source;
    private Long jobCardId;
    private OffsetDateTime createdAt;

    // Set only on the response to logging a visit: whether a new customer / vehicle was created.
    private boolean createdCustomer;
    private boolean createdVehicle;
}
