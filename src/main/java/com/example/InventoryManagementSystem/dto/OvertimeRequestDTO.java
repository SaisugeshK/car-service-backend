package com.example.InventoryManagementSystem.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class OvertimeRequestDTO {

    @NotNull(message = "userId is required")
    private Long userId;

    @NotNull(message = "workDate is required")
    private LocalDate workDate;

    @NotNull(message = "hours is required")
    @DecimalMin(value = "0.01", message = "hours must be greater than zero")
    private BigDecimal hours;

    @NotNull(message = "rate is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "rate cannot be negative")
    private BigDecimal rate;

    private String notes;
}
