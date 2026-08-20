package com.example.InventoryManagementSystem.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "job_cards")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class JobCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long jobCardId;

    @Column(unique = true)
    private String jobCardNumber;

    @Column(nullable = false)
    private Long customerId;

    @Column(nullable = false)
    private Long vehicleId;

    private Long advisorUserId;
    private Long technicianUserId;

    private Long appointmentId;
    private Long estimateId;
    private Long invoiceId;

    private OffsetDateTime dateIn = OffsetDateTime.now();
    private OffsetDateTime expectedDelivery;

    private Integer odometer;
    private String fuelLevel;

    @Column(columnDefinition = "TEXT")
    private String complaint;

    private Boolean keysReceived = false;

    @Column(columnDefinition = "TEXT")
    private String accessoriesReceived;

    @Column(columnDefinition = "TEXT")
    private String workRequired;

    @Column(columnDefinition = "TEXT")
    private String internalNotes;

    // Structured intake condition notes (no file-upload infra yet — text-based damage list
    // instead of photos, e.g. "Front bumper scratch; Rear door dent").
    @Column(columnDefinition = "TEXT")
    private String vehicleConditionNotes;

    // RECEIVED -> INSPECTION -> ESTIMATE -> WAITING_APPROVAL -> APPROVED -> IN_PROGRESS ->
    // WAITING_FOR_PARTS -> QUALITY_CHECK -> READY_FOR_DELIVERY -> DELIVERED (+ CANCELLED)
    private String status = "RECEIVED";

    private OffsetDateTime deliveredAt;

    private OffsetDateTime createdAt = OffsetDateTime.now();
    private OffsetDateTime updatedAt = OffsetDateTime.now();

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }
}
