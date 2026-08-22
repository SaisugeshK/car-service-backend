package com.example.InventoryManagementSystem.dto;

import lombok.Data;

// Confirmed by staff before a job card can move READY_FOR_DELIVERY -> DELIVERED. Service
// completed / quality check completed / invoice generated / payment status / customer approval
// are all shown on the frontend checklist but derived from existing data (job card status, QC
// history, invoice), not re-confirmed here — these three plus who's delivering are the items
// with no other source of truth, so the server requires them explicitly.
@Data
public class DeliveryChecklistDTO {
    private Long deliveredByUserId;
    private Boolean vehicleCleaned;
    private Boolean belongingsChecked;
    private Boolean keysReady;
}
