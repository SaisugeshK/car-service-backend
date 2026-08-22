package com.example.InventoryManagementSystem.service;

import com.example.InventoryManagementSystem.dto.AuditLogResponseDTO;

import java.util.List;

public interface AuditLogService {

    // Fire-and-forget, same convention as NotificationEventService.raise — records the current
    // authenticated user automatically via CurrentUserService, so every call site only needs to
    // say what happened, not who did it.
    void record(String action, String entityType, Long entityId, String description);

    List<AuditLogResponseDTO> getAll();

    List<AuditLogResponseDTO> getByEntityType(String entityType);
}
