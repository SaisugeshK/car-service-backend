package com.example.InventoryManagementSystem.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

// One customer visit to the workshop. Drives the customer's visit count and New / Occasional /
// Regular status (CustomerVisitStatsService). Logged by staff from the Visits page, or created
// automatically for every job card (source JOB_CARD, linked by jobCardId).
@Entity
@Table(name = "visits")
@Getter
@Setter
@NoArgsConstructor
public class Visit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long visitId;

    @Column(nullable = false)
    private Long customerId;

    private Long vehicleId;

    @Column(nullable = false)
    private OffsetDateTime visitDateTime;

    // SERVICE / REPAIR / INQUIRY / INSURANCE_RENEWAL / OTHER
    @Column(nullable = false, length = 30)
    private String purpose;

    @Column(columnDefinition = "TEXT")
    private String notes;

    // Staff member who handled the visit (snapshotted name so history stays readable).
    private Long handledByUserId;
    private String handledByName;

    // MANUAL (logged on the Visits page) or JOB_CARD (created with a job card).
    @Column(nullable = false, length = 20)
    private String source = "MANUAL";

    @Column(unique = true)
    private Long jobCardId;

    @Column(updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    public void prePersist() {
        createdAt = OffsetDateTime.now();
        if (visitDateTime == null) visitDateTime = createdAt;
    }
}
