package com.example.InventoryManagementSystem.controllor;

import com.example.InventoryManagementSystem.dto.EstimateRequestDTO;
import com.example.InventoryManagementSystem.dto.EstimateResponseDTO;
import com.example.InventoryManagementSystem.service.EstimateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/estimates")
@RequiredArgsConstructor
public class EstimateController {

    private final EstimateService service;

    @PostMapping
    public ResponseEntity<EstimateResponseDTO> create(@Valid @RequestBody EstimateRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createEstimate(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EstimateResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getEstimateById(id));
    }

    @GetMapping
    public ResponseEntity<List<EstimateResponseDTO>> getAll() {
        return ResponseEntity.ok(service.getAllEstimates());
    }

    @GetMapping("/job-card/{jobCardId}")
    public ResponseEntity<List<EstimateResponseDTO>> getByJobCard(@PathVariable Long jobCardId) {
        return ResponseEntity.ok(service.getByJobCardId(jobCardId));
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<EstimateResponseDTO> approve(@PathVariable Long id, @RequestBody(required = false) Map<String, String> body) {
        String approvedBy = body != null ? body.get("approvedBy") : null;
        return ResponseEntity.ok(service.approve(id, approvedBy));
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<EstimateResponseDTO> reject(@PathVariable Long id, @RequestBody(required = false) Map<String, String> body) {
        String notes = body != null ? body.get("notes") : null;
        return ResponseEntity.ok(service.reject(id, notes));
    }

    @PostMapping("/{id}/request-changes")
    public ResponseEntity<EstimateResponseDTO> requestChanges(@PathVariable Long id, @RequestBody(required = false) Map<String, String> body) {
        String notes = body != null ? body.get("notes") : null;
        return ResponseEntity.ok(service.requestChanges(id, notes));
    }

    @PostMapping("/{id}/revise")
    public ResponseEntity<EstimateResponseDTO> revise(@PathVariable Long id, @Valid @RequestBody EstimateRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.reviseEstimate(id, dto));
    }

    @GetMapping("/{id}/revisions")
    public ResponseEntity<List<EstimateResponseDTO>> getRevisions(@PathVariable Long id) {
        return ResponseEntity.ok(service.getRevisions(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        service.deleteEstimate(id);
        return ResponseEntity.ok("Estimate deleted successfully");
    }
}
