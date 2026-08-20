package com.example.InventoryManagementSystem.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;

@Entity
@Table(name = "appointments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long appointmentId;

    @Column(nullable = false)
    private Long customerId;

    private Long vehicleId;

    private String phone;

    @Column(nullable = false)
    private LocalDate appointmentDate;

    private LocalTime appointmentTime;

    private String requestedService;

    @Column(columnDefinition = "TEXT")
    private String notes;

    private Long advisorUserId;

    // BOOKED / CONFIRMED / ARRIVED / NO_SHOW / CANCELLED / COMPLETED
    private String status = "BOOKED";

    // Set once this appointment is converted into a Job Card, so it isn't converted twice.
    private Long jobCardId;

    private OffsetDateTime createdAt = OffsetDateTime.now();
    private OffsetDateTime updatedAt = OffsetDateTime.now();

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }
}
