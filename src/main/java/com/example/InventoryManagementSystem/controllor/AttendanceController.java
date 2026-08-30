package com.example.InventoryManagementSystem.controllor;

import com.example.InventoryManagementSystem.dto.AttendanceRequestDTO;
import com.example.InventoryManagementSystem.dto.AttendanceResponseDTO;
import com.example.InventoryManagementSystem.service.AttendanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService service;

    @PostMapping
    public ResponseEntity<AttendanceResponseDTO> create(@Valid @RequestBody AttendanceRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AttendanceResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<AttendanceResponseDTO>> getAll(@RequestParam(required = false) Long userId) {
        return ResponseEntity.ok(userId != null ? service.getByUserId(userId) : service.getAll());
    }

    @PutMapping("/{id}")
    public ResponseEntity<AttendanceResponseDTO> update(@PathVariable Long id, @RequestBody AttendanceRequestDTO dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok("Attendance record deleted successfully");
    }
}
