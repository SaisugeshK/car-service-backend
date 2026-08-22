package com.example.InventoryManagementSystem.controllor;

import com.example.InventoryManagementSystem.dto.NotificationLogResponseDTO;
import com.example.InventoryManagementSystem.dto.NotificationSendRequestDTO;
import com.example.InventoryManagementSystem.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService service;

    // Always 200s with a status field (NOT_CONFIGURED / FAILED / SENT / DELIVERED) — never a fake
    // success. The caller reads response.status, not the HTTP status code, to know what happened.
    @PostMapping("/send")
    public ResponseEntity<NotificationLogResponseDTO> send(@Valid @RequestBody NotificationSendRequestDTO dto) {
        return ResponseEntity.ok(service.send(dto));
    }

    @GetMapping("/reference/{referenceType}/{referenceId}")
    public ResponseEntity<List<NotificationLogResponseDTO>> getByReference(
            @PathVariable String referenceType, @PathVariable Long referenceId) {
        return ResponseEntity.ok(service.getByReference(referenceType, referenceId));
    }

    @GetMapping
    public ResponseEntity<List<NotificationLogResponseDTO>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }
}
