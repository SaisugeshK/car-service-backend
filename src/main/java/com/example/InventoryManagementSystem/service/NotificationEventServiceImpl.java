package com.example.InventoryManagementSystem.service;

import com.example.InventoryManagementSystem.Repository.NotificationEventRepository;
import com.example.InventoryManagementSystem.dto.NotificationEventResponseDTO;
import com.example.InventoryManagementSystem.exception.ResourceNotFoundException;
import com.example.InventoryManagementSystem.model.NotificationEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationEventServiceImpl implements NotificationEventService {

    private final NotificationEventRepository repository;

    @Override
    public void raise(String type, String title, String message, String referenceType, Long referenceId) {
        // Best-effort by design: a notification failing to save must never break the real
        // business action (e.g. an estimate approval) that triggered it — see every call site.
        try {
            NotificationEvent event = NotificationEvent.builder()
                    .type(type)
                    .title(title)
                    .message(message)
                    .referenceType(referenceType)
                    .referenceId(referenceId)
                    .isRead(false)
                    .build();
            repository.save(event);
        } catch (Exception ignored) {
            // Swallow — see comment above.
        }
    }

    @Override
    public List<NotificationEventResponseDTO> getAll() {
        return repository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public long getUnreadCount() {
        return repository.countByIsReadFalse();
    }

    @Override
    @Transactional
    public NotificationEventResponseDTO markRead(Long id) {
        NotificationEvent event = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " + id));
        event.setIsRead(true);
        return mapToDto(repository.save(event));
    }

    @Override
    @Transactional
    public void markAllRead() {
        List<NotificationEvent> unread = repository.findAllByOrderByCreatedAtDesc().stream()
                .filter(e -> !Boolean.TRUE.equals(e.getIsRead()))
                .collect(Collectors.toList());
        unread.forEach(e -> e.setIsRead(true));
        repository.saveAll(unread);
    }

    private NotificationEventResponseDTO mapToDto(NotificationEvent event) {
        return NotificationEventResponseDTO.builder()
                .notificationEventId(event.getNotificationEventId())
                .type(event.getType())
                .title(event.getTitle())
                .message(event.getMessage())
                .referenceType(event.getReferenceType())
                .referenceId(event.getReferenceId())
                .isRead(event.getIsRead())
                .createdAt(event.getCreatedAt())
                .build();
    }
}
