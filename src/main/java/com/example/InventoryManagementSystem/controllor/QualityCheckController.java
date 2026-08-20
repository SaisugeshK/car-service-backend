package com.example.InventoryManagementSystem.controllor;

import com.example.InventoryManagementSystem.dto.QualityCheckRequestDTO;
import com.example.InventoryManagementSystem.dto.QualityCheckResponseDTO;
import com.example.InventoryManagementSystem.service.QualityCheckService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/quality-checks")
@RequiredArgsConstructor
public class QualityCheckController {

    private final QualityCheckService service;

    @PostMapping
    public ResponseEntity<QualityCheckResponseDTO> record(@Valid @RequestBody QualityCheckRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.recordCheck(dto));
    }

    @GetMapping("/job-card/{jobCardId}")
    public ResponseEntity<List<QualityCheckResponseDTO>> getByJobCard(@PathVariable Long jobCardId) {
        return ResponseEntity.ok(service.getByJobCardId(jobCardId));
    }
}
