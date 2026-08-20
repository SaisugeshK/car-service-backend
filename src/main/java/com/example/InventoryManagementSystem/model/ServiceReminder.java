package com.example.InventoryManagementSystem.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "service_reminders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ServiceReminder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reminderId;

    @Column(nullable = false)
    private Long vehicleId;

    private LocalDate dueDate;
    private Integer dueOdometer;

    private Long sourceInvoiceId;

    @Column(columnDefinition = "TEXT")
    private String notes;

    // UPCOMING / DUE / OVERDUE / DONE — DONE set once the vehicle returns for the next service.
    private String status = "UPCOMING";

    private OffsetDateTime createdAt = OffsetDateTime.now();
}
