package com.example.InventoryManagementSystem.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

// HRM/payroll — one row per employee per overtime shift. Only APPROVED entries are ever read
// by PayrollCalculationService and added to gross pay; PENDING/REJECTED never affect pay.
@Entity
@Table(name = "overtime_entries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OvertimeEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long overtimeId;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private LocalDate workDate;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal hours;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal rate;

    // hours * rate, computed server-side — never trusted from the client.
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    // PENDING | APPROVED | REJECTED
    @Builder.Default
    private String status = "PENDING";

    private Long approvedByUserId;
    private OffsetDateTime approvedAt;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Builder.Default
    private OffsetDateTime createdAt = OffsetDateTime.now();
    @Builder.Default
    private OffsetDateTime updatedAt = OffsetDateTime.now();

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }
}
