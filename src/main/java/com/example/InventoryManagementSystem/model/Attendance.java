package com.example.InventoryManagementSystem.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.OffsetDateTime;

// HRM/payroll — one row per employee per calendar day. Feeds PayrollCalculationService's
// attendance-deduction math; see that class for the day-resolution rule (Leave takes
// precedence over Attendance for a given date, so the two never double-count).
@Entity
@Table(name = "attendance", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "attendance_date"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long attendanceId;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private LocalDate attendanceDate;

    // PRESENT | ABSENT | WEEK_OFF | HOLIDAY
    @Column(nullable = false)
    private String status;

    private Long markedByUserId;

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
