package com.example.InventoryManagementSystem.dto;

import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@Builder
public class AuditLogResponseDTO {
    private Long auditLogId;
    private Long userId;
    private String username;
    private String action;
    private String entityType;
    private Long entityId;
    private String description;
    private OffsetDateTime createdAt;
}
