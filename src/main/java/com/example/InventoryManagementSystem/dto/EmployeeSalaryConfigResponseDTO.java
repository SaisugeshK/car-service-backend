package com.example.InventoryManagementSystem.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Data
public class EmployeeSalaryConfigResponseDTO {

    private Long salaryConfigId;
    private Long userId;
    private String userName;
    private String roleName;
    private BigDecimal basicPay;
    private BigDecimal hra;
    private BigDecimal otherAllowances;
    private BigDecimal deductions;
    private LocalDate effectiveFrom;
    private Boolean active;
    private String notes;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
