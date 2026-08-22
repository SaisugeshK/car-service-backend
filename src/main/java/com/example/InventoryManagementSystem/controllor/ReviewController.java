package com.example.InventoryManagementSystem.controllor;

import com.example.InventoryManagementSystem.dto.ReviewRequestDTO;
import com.example.InventoryManagementSystem.dto.ReviewResponseDTO;
import com.example.InventoryManagementSystem.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService service;

    @PostMapping
    public ResponseEntity<ReviewResponseDTO> create(@Valid @RequestBody ReviewRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReviewResponseDTO> update(@PathVariable Long id, @RequestBody ReviewRequestDTO dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @GetMapping
    public ResponseEntity<List<ReviewResponseDTO>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReviewResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok("Review deleted successfully");
    }
}
