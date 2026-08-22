package com.example.InventoryManagementSystem.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Data
public class ServiceReminderResponseDTO {

    private Long reminderId;
    private Long vehicleId;
    private String vehicleModel;
    private String registrationNumber;
    private Long customerId;
    private String customerName;
    private String customerPhone;
    private String customerWhatsapp;

    private String reminderType;
    private LocalDate dueDate;
    private Integer dueOdometer;
    private Long sourceInvoiceId;
    private String notes;
    private String status;
    private OffsetDateTime createdAt;
}
