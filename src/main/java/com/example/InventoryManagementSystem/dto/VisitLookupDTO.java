package com.example.InventoryManagementSystem.dto;

import lombok.Data;

import java.time.OffsetDateTime;

// What the Log Visit form shows as the user types a mobile / registration number: who and what
// the visit will be linked to, before it's saved.
@Data
public class VisitLookupDTO {
    private Long customerId;
    private String customerName;
    private String customerPhone;
    private String regularStatus;
    private Integer totalVisits;
    private OffsetDateTime lastVisitDate;

    private Long vehicleId;
    private String vehicleModel;
    private String registrationNumber;

    // The registration belongs to a different customer than the one the mobile number matched —
    // the visit goes to the vehicle's owner.
    private boolean ownerMismatch;
    private String vehicleOwnerName;
}
