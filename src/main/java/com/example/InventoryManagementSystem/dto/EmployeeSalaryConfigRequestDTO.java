package com.example.InventoryManagementSystem.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class EmployeeSalaryConfigRequestDTO {

    @NotNull(message = "userId is required")
    private Long userId;

    @NotNull(message = "basicPay is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "basicPay cannot be negative")
    private BigDecimal basicPay;

    @DecimalMin(value = "0.0", inclusive = true, message = "hra cannot be negative")
    private BigDecimal hra;

    @DecimalMin(value = "0.0", inclusive = true, message = "otherAllowances cannot be negative")
    private BigDecimal otherAllowances;

    @DecimalMin(value = "0.0", inclusive = true, message = "deductions cannot be negative")
    private BigDecimal deductions;

    @NotNull(message = "effectiveFrom is required")
    private LocalDate effectiveFrom;

    private Boolean active;
    private String notes;
}
