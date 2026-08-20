package com.example.InventoryManagementSystem.controllor;

import com.example.InventoryManagementSystem.dto.SalesReturnRequestDTO;
import com.example.InventoryManagementSystem.dto.SalesReturnResponseDTO;
import com.example.InventoryManagementSystem.service.SalesReturnService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sales-returns")
@RequiredArgsConstructor
public class SalesReturnController {

    private final SalesReturnService salesReturnService;

    // CREATE
    @PostMapping
    public ResponseEntity<SalesReturnResponseDTO> create(
            @RequestBody SalesReturnRequestDTO dto) {

        return ResponseEntity.ok(
                salesReturnService.createReturn(dto));
    }

    // GET BY ID
    @GetMapping("/{id}")
    public ResponseEntity<SalesReturnResponseDTO> getById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                salesReturnService.getById(id));
    }

    // GET ALL
    @GetMapping
    public ResponseEntity<List<SalesReturnResponseDTO>>
    getAll() {

        return ResponseEntity.ok(
                salesReturnService.getAll());
    }

    // UPDATE
    @PutMapping("/{id}")
    public ResponseEntity<SalesReturnResponseDTO>
    updateReturn(
            @PathVariable Long id,
            @RequestBody SalesReturnRequestDTO dto) {

        return ResponseEntity.ok(
                salesReturnService.updateReturn(
                        id,
                        dto));
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(
            @PathVariable Long id) {

        salesReturnService.delete(id);

        return ResponseEntity.ok(
                "Sales return deleted successfully");
    }
}