package com.example.InventoryManagementSystem.service;

import com.example.InventoryManagementSystem.Repository.AuditLogRepository;
import com.example.InventoryManagementSystem.dto.AuditLogResponseDTO;
import com.example.InventoryManagementSystem.model.AuditLog;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository repository;
    private final CurrentUserService currentUserService;

    @Override
    public void record(String action, String entityType, Long entityId, String description) {
        // Best-effort by design, same reasoning as NotificationEventService.raise — a failure to
        // write an audit row must never break the real business action that triggered it.
        try {
            AuditLog log = AuditLog.builder()
                    .userId(currentUserService.getCurrentUserId())
                    .username(currentUserService.getCurrentUsername())
                    .action(action)
                    .entityType(entityType)
                    .entityId(entityId)
                    .description(description)
                    .build();
            repository.save(log);
        } catch (Exception ignored) {
            // Swallow — see comment above.
        }
    }

    @Override
    public List<AuditLogResponseDTO> getAll() {
        return repository.findAllByOrderByCreatedAtDesc().stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Override
    public List<AuditLogResponseDTO> getByEntityType(String entityType) {
        return repository.findByEntityTypeOrderByCreatedAtDesc(entityType).stream().map(this::mapToDto).collect(Collectors.toList());
    }

    private AuditLogResponseDTO mapToDto(AuditLog log) {
        return AuditLogResponseDTO.builder()
                .auditLogId(log.getAuditLogId())
                .userId(log.getUserId())
                .username(log.getUsername())
                .action(log.getAction())
                .entityType(log.getEntityType())
                .entityId(log.getEntityId())
                .description(log.getDescription())
                .createdAt(log.getCreatedAt())
                .build();
    }
}
