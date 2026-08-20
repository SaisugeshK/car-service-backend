package com.example.InventoryManagementSystem.controllor;

import com.example.InventoryManagementSystem.dto.InspectionItemRequestDTO;
import com.example.InventoryManagementSystem.dto.InspectionItemResponseDTO;
import com.example.InventoryManagementSystem.service.InspectionItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inspection-items")
@RequiredArgsConstructor
public class InspectionItemController {

    private final InspectionItemService service;

    // Upsert — same endpoint handles first entry and later edits for a category.
    @PostMapping
    public ResponseEntity<InspectionItemResponseDTO> save(@Valid @RequestBody InspectionItemRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.saveInspectionItem(dto));
    }

    @GetMapping("/job-card/{jobCardId}")
    public ResponseEntity<List<InspectionItemResponseDTO>> getByJobCard(@PathVariable Long jobCardId) {
        return ResponseEntity.ok(service.getByJobCardId(jobCardId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        service.deleteInspectionItem(id);
        return ResponseEntity.ok("Inspection item deleted successfully");
    }
}
