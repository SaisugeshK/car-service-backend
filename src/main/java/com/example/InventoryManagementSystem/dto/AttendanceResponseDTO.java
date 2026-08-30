package com.example.InventoryManagementSystem.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Data
public class AttendanceResponseDTO {

    private Long attendanceId;
    private Long userId;
    private String userName;
    private LocalDate attendanceDate;
    private String status;
    private Long markedByUserId;
    private String markedByName;
    private String notes;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
