package com.example.InventoryManagementSystem.controllor;

import com.example.InventoryManagementSystem.dto.AdditionalWorkRequestDTO;
import com.example.InventoryManagementSystem.dto.AdditionalWorkResponseDTO;
import com.example.InventoryManagementSystem.service.AdditionalWorkService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/additional-work")
@RequiredArgsConstructor
public class AdditionalWorkController {

    private final AdditionalWorkService service;

    @PostMapping
    public ResponseEntity<AdditionalWorkResponseDTO> create(@Valid @RequestBody AdditionalWorkRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    @GetMapping("/job-card/{jobCardId}")
    public ResponseEntity<List<AdditionalWorkResponseDTO>> getByJobCard(@PathVariable Long jobCardId) {
        return ResponseEntity.ok(service.getByJobCard(jobCardId));
    }

    @GetMapping
    public ResponseEntity<List<AdditionalWorkResponseDTO>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<AdditionalWorkResponseDTO> approve(@PathVariable Long id, @RequestBody(required = false) Map<String, String> body) {
        String decidedBy = body != null ? body.get("decidedBy") : null;
        return ResponseEntity.ok(service.approve(id, decidedBy));
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<AdditionalWorkResponseDTO> reject(@PathVariable Long id, @RequestBody(required = false) Map<String, String> body) {
        String decidedBy = body != null ? body.get("decidedBy") : null;
        return ResponseEntity.ok(service.reject(id, decidedBy));
    }
}
