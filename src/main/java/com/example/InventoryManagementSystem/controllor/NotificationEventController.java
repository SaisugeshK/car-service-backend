package com.example.InventoryManagementSystem.controllor;

import com.example.InventoryManagementSystem.dto.NotificationEventResponseDTO;
import com.example.InventoryManagementSystem.service.NotificationEventService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notification-events")
@RequiredArgsConstructor
public class NotificationEventController {

    private final NotificationEventService service;

    @GetMapping
    public ResponseEntity<List<NotificationEventResponseDTO>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount() {
        return ResponseEntity.ok(Map.of("unreadCount", service.getUnreadCount()));
    }

    @PostMapping("/{id}/read")
    public ResponseEntity<NotificationEventResponseDTO> markRead(@PathVariable Long id) {
        return ResponseEntity.ok(service.markRead(id));
    }

    @PostMapping("/read-all")
    public ResponseEntity<Void> markAllRead() {
        service.markAllRead();
        return ResponseEntity.ok().build();
    }
}
