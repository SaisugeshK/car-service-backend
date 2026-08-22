package com.example.InventoryManagementSystem.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "complaints")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Complaint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long complaintId;

    @Column(nullable = false)
    private Long customerId;

    private Long vehicleId;
    private Long jobCardId;

    // SERVICE_QUALITY / BILLING / DELAY / STAFF_BEHAVIOR / PARTS / OTHER — plain string, not a
    // hard enum, same flexibility convention as itemType/discountType elsewhere.
    private String type;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    // LOW / MEDIUM / HIGH / URGENT
    private String priority = "MEDIUM";

    private Long assignedToUserId;

    // OPEN / IN_PROGRESS / RESOLVED / CLOSED
    private String status = "OPEN";

    @Column(columnDefinition = "TEXT")
    private String resolution;

    private LocalDate resolutionDate;

    private OffsetDateTime createdAt = OffsetDateTime.now();
}
