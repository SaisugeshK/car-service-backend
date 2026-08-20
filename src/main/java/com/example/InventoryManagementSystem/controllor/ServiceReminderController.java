package com.example.InventoryManagementSystem.controllor;

import com.example.InventoryManagementSystem.dto.ServiceReminderRequestDTO;
import com.example.InventoryManagementSystem.dto.ServiceReminderResponseDTO;
import com.example.InventoryManagementSystem.service.ServiceReminderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/service-reminders")
@RequiredArgsConstructor
public class ServiceReminderController {

    private final ServiceReminderService service;

    @PostMapping
    public ResponseEntity<ServiceReminderResponseDTO> create(@Valid @RequestBody ServiceReminderRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createReminder(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServiceReminderResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getReminderById(id));
    }

    @GetMapping
    public ResponseEntity<List<ServiceReminderResponseDTO>> getAll() {
        return ResponseEntity.ok(service.getAllReminders());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ServiceReminderResponseDTO> update(@PathVariable Long id, @RequestBody ServiceReminderRequestDTO dto) {
        return ResponseEntity.ok(service.updateReminder(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        service.deleteReminder(id);
        return ResponseEntity.ok("Service reminder deleted successfully");
    }
}
