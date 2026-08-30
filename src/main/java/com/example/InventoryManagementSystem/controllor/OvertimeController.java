package com.example.InventoryManagementSystem.controllor;

import com.example.InventoryManagementSystem.dto.OvertimeRequestDTO;
import com.example.InventoryManagementSystem.dto.OvertimeResponseDTO;
import com.example.InventoryManagementSystem.service.OvertimeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/overtime")
@RequiredArgsConstructor
public class OvertimeController {

    private final OvertimeService service;

    @PostMapping
    public ResponseEntity<OvertimeResponseDTO> create(@Valid @RequestBody OvertimeRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OvertimeResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<OvertimeResponseDTO>> getAll(@RequestParam(required = false) Long userId) {
        return ResponseEntity.ok(userId != null ? service.getByUserId(userId) : service.getAll());
    }

    @PutMapping("/{id}")
    public ResponseEntity<OvertimeResponseDTO> update(@PathVariable Long id, @RequestBody OvertimeRequestDTO dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<OvertimeResponseDTO> approve(@PathVariable Long id) {
        return ResponseEntity.ok(service.approve(id));
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<OvertimeResponseDTO> reject(@PathVariable Long id) {
        return ResponseEntity.ok(service.reject(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok("Overtime entry deleted successfully");
    }
}
