package com.example.InventoryManagementSystem.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "estimates")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Estimate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long estimateId;

    // NOT unique — every revision of the same estimate (REV 1, REV 2, REV 3...) shares this same
    // number, distinguished by revisionNumber instead. Uniqueness lived here before revisions
    // existed; kept as a plain indexed column now.
    @Column
    private String estimateNumber;

    @Column(nullable = false)
    private Long jobCardId;

    @Column(nullable = false)
    private Long customerId;

    // Estimate date is createdAt below; this is the separate "quote expires on" date shown to
    // the customer — no automated expiry job yet, just a date the UI can compare against.
    private java.time.LocalDate validUntil;

    private BigDecimal subtotal = BigDecimal.ZERO;
    private BigDecimal discountAmount = BigDecimal.ZERO;
    private BigDecimal taxAmount = BigDecimal.ZERO;
    private BigDecimal grandTotal = BigDecimal.ZERO;

    // PENDING / APPROVED / REJECTED / CHANGES_REQUESTED
    private String status = "PENDING";

    // Revision chain: rootEstimateId is null on the first revision (REV 1) — its own id IS the
    // root. Every later revision points at that same root id, so "all revisions of EST-1001" is
    // one query regardless of how many times it's been revised. Revising never edits or deletes
    // an existing row — it always inserts a new one, so every past revision stays exactly as it
    // was when the customer saw it (audit trail, per spec).
    private Long rootEstimateId;
    private Integer revisionNumber = 1;

    private OffsetDateTime approvedDate;
    private String approvedBy;

    @Column(columnDefinition = "TEXT")
    private String notes;

    private OffsetDateTime createdAt = OffsetDateTime.now();
    private OffsetDateTime updatedAt = OffsetDateTime.now();

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }
}
