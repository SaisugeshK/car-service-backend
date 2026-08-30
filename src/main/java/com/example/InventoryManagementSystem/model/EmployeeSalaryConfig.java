package com.example.InventoryManagementSystem.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

// HRM/payroll — an employee's *current* pay terms, one active row per user_id (not a history
// table: a raise is a plain update in place). PayrollCalculationService snapshots these values
// onto SalaryPayment at generation time, so editing this later never changes an already-generated
// month's pay.
@Entity
@Table(name = "employee_salary_config")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeSalaryConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long salaryConfigId;

    @Column(nullable = false, unique = true)
    private Long userId;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal basicPay;

    @Builder.Default
    @Column(precision = 12, scale = 2)
    private BigDecimal hra = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "other_allowances", precision = 12, scale = 2)
    private BigDecimal otherAllowances = BigDecimal.ZERO;

    @Builder.Default
    @Column(precision = 12, scale = 2)
    private BigDecimal deductions = BigDecimal.ZERO;

    @Column(nullable = false)
    private LocalDate effectiveFrom;

    @Builder.Default
    private Boolean active = true;

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
