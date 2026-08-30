package com.example.InventoryManagementSystem.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Data
public class OvertimeResponseDTO {

    private Long overtimeId;
    private Long userId;
    private String userName;
    private LocalDate workDate;
    private BigDecimal hours;
    private BigDecimal rate;
    private BigDecimal amount;
    private String status;
    private Long approvedByUserId;
    private String approvedByName;
    private OffsetDateTime approvedAt;
    private String notes;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
