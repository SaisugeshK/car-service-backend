package com.example.InventoryManagementSystem.service;

import com.example.InventoryManagementSystem.Repository.NotificationLogRepository;
import com.example.InventoryManagementSystem.dto.NotificationLogResponseDTO;
import com.example.InventoryManagementSystem.dto.NotificationSendRequestDTO;
import com.example.InventoryManagementSystem.model.NotificationLog;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

// No WhatsApp/SMS provider (Twilio, Gupshup, etc.) is integrated into this backend. Every send
// attempt is still logged honestly — status is NOT_CONFIGURED (or FAILED, if there's no recipient
// number at all) rather than pretending delivery happened. Wiring a real provider here later is a
// self-contained change: resolve credentials, call the provider SDK/API, set status to
// SENT/FAILED based on the real response — nothing else in the app needs to change.
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationLogRepository repository;

    @Override
    public NotificationLogResponseDTO send(NotificationSendRequestDTO dto) {
        NotificationLog log = new NotificationLog();
        log.setChannel(normalizeChannel(dto.getChannel()));
        log.setRecipientPhone(dto.getRecipientPhone());
        log.setReferenceType(dto.getReferenceType());
        log.setReferenceId(dto.getReferenceId());
        log.setSubject(dto.getSubject());
        log.setMessage(dto.getMessage());

        if (dto.getRecipientPhone() == null || dto.getRecipientPhone().isBlank()) {
            log.setStatus("FAILED");
            log.setErrorMessage("No recipient phone number on file for this customer");
        } else {
            log.setStatus("NOT_CONFIGURED");
            log.setErrorMessage("No WhatsApp/SMS provider is configured on the server");
        }

        return mapToDto(repository.save(log));
    }

    @Override
    public List<NotificationLogResponseDTO> getByReference(String referenceType, Long referenceId) {
        return repository.findByReferenceTypeAndReferenceIdOrderByCreatedAtDesc(referenceType, referenceId)
                .stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Override
    public List<NotificationLogResponseDTO> getAll() {
        return repository.findAllByOrderByCreatedAtDesc().stream().map(this::mapToDto).collect(Collectors.toList());
    }

    private String normalizeChannel(String channel) {
        if (channel == null || channel.isBlank()) {
            throw new IllegalArgumentException("channel is required (WHATSAPP or SMS)");
        }
        String upper = channel.trim().toUpperCase();
        if (!upper.equals("WHATSAPP") && !upper.equals("SMS")) {
            throw new IllegalArgumentException("channel must be WHATSAPP or SMS");
        }
        return upper;
    }

    private NotificationLogResponseDTO mapToDto(NotificationLog log) {
        NotificationLogResponseDTO dto = new NotificationLogResponseDTO();
        dto.setNotificationLogId(log.getNotificationLogId());
        dto.setChannel(log.getChannel());
        dto.setRecipientPhone(log.getRecipientPhone());
        dto.setReferenceType(log.getReferenceType());
        dto.setReferenceId(log.getReferenceId());
        dto.setSubject(log.getSubject());
        dto.setMessage(log.getMessage());
        dto.setStatus(log.getStatus());
        dto.setErrorMessage(log.getErrorMessage());
        dto.setCreatedAt(log.getCreatedAt());
        return dto;
    }
}
