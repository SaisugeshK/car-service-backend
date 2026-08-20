package com.example.InventoryManagementSystem.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ServiceReminderRequestDTO {

    @NotNull(message = "vehicleId is required")
    private Long vehicleId;

    private LocalDate dueDate;
    private Integer dueOdometer;
    private Long sourceInvoiceId;
    private String notes;
    private String status;
}
