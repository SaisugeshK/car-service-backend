package com.example.InventoryManagementSystem.dto;

import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class JobCardStatusHistoryResponseDTO {
    private String status;
    private OffsetDateTime changedAt;
}
