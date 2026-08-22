package com.example.InventoryManagementSystem.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

// Work a technician discovers mid-service that was NOT part of the original approved estimate —
// e.g. a worn wheel bearing found while doing a brake job. Never auto-billed: this is its own
// approve/reject cycle, separate from the Estimate one, and only APPROVED rows are ever eligible
// to be pulled into the final invoice (see JobCardServiceImpl.generateInvoice).
@Entity
@Table(name = "additional_work_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AdditionalWorkRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long additionalWorkRequestId;

    @Column(nullable = false)
    private Long jobCardId;

    // Who found it and asked for approval — the technician (or advisor) working the job.
    private Long requestedByUserId;

    @Column(columnDefinition = "TEXT")
    private String notes;

    private BigDecimal subtotal = BigDecimal.ZERO;
    private BigDecimal discountAmount = BigDecimal.ZERO;
    private BigDecimal taxAmount = BigDecimal.ZERO;
    private BigDecimal grandTotal = BigDecimal.ZERO;

    // PENDING / APPROVED / REJECTED
    private String status = "PENDING";

    private OffsetDateTime requestedAt = OffsetDateTime.now();
    private OffsetDateTime decidedAt;
    private String decidedBy;
}
