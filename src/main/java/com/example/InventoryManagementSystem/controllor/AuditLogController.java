package com.example.InventoryManagementSystem.controllor;

import com.example.InventoryManagementSystem.dto.AuditLogResponseDTO;
import com.example.InventoryManagementSystem.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/audit-logs")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditLogService service;

    @GetMapping
    public ResponseEntity<List<AuditLogResponseDTO>> getAll(@RequestParam(required = false) String entityType) {
        if (entityType != null && !entityType.isBlank()) {
            return ResponseEntity.ok(service.getByEntityType(entityType.toUpperCase()));
        }
        return ResponseEntity.ok(service.getAll());
    }
}
