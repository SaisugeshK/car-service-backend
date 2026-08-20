package com.example.InventoryManagementSystem.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "customer_follow_ups")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CustomerFollowUp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long followUpId;

    @Column(nullable = false)
    private Long customerId;

    private Long vehicleId;

    private LocalDate reminderDate;

    @Column(columnDefinition = "TEXT")
    private String customerResponse;

    // PENDING / CONTACTED / BOOKED / COMPLETED / NO_RESPONSE
    private String status = "PENDING";

    @Column(columnDefinition = "TEXT")
    private String notes;

    private OffsetDateTime createdAt = OffsetDateTime.now();
    private OffsetDateTime updatedAt = OffsetDateTime.now();

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }
}
