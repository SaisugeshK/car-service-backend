package com.example.InventoryManagementSystem.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MarkSalaryPaidRequestDTO {

    @NotNull(message = "paymentMethod is required")
    private String paymentMethod;

    // Required for BANK_TRANSFER/UPI/CHEQUE, optional for CASH — enforced in PayrollServiceImpl
    // (spec §17), not here, since the rule depends on paymentMethod's value.
    private String paymentReference;
}
