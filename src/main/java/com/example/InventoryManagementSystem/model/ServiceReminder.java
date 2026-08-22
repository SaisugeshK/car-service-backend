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

    // NEXT_SERVICE / OIL_CHANGE / INSURANCE_EXPIRY / PUC_EXPIRY / TYRE_REPLACEMENT / BATTERY /
    // GENERAL_SERVICE. Defaults to NEXT_SERVICE so every reminder created before this field
    // existed keeps meaning exactly what it always meant.
    private String reminderType = "NEXT_SERVICE";

    private LocalDate dueDate;
    private Integer dueOdometer;

    private Long sourceInvoiceId;

    @Column(columnDefinition = "TEXT")
    private String notes;

    // UPCOMING / DUE / OVERDUE / DONE — DONE set once the vehicle returns for the next service.
    private String status = "UPCOMING";

    private OffsetDateTime createdAt = OffsetDateTime.now();
}
