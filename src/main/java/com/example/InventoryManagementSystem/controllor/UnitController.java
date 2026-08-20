package com.example.InventoryManagementSystem.controllor;

import com.example.InventoryManagementSystem.dto.UnitRequestDto;
import com.example.InventoryManagementSystem.dto.UnitResponseDto;
import com.example.InventoryManagementSystem.service.UnitService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/units")
@RequiredArgsConstructor
public class UnitController {

    private final UnitService unitService;

    // CREATE
    @PostMapping
    public ResponseEntity<UnitResponseDto>
    createUnit(
            @RequestBody UnitRequestDto dto) {

        return ResponseEntity.ok(
                unitService.createUnit(dto));
    }

    // GET ALL
    @GetMapping
    public ResponseEntity<List<UnitResponseDto>>
    getAllUnits() {

        return ResponseEntity.ok(
                unitService.getAllUnits());
    }

    // GET BY ID
    @GetMapping("/{id}")
    public ResponseEntity<UnitResponseDto>
    getUnitById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                unitService.getUnitById(id));
    }

    // UPDATE
    @PutMapping("/{id}")
    public ResponseEntity<UnitResponseDto>
    updateUnit(
            @PathVariable Long id,
            @RequestBody UnitRequestDto dto) {

        return ResponseEntity.ok(
                unitService.updateUnit(id, dto));
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<String>
    deleteUnit(
            @PathVariable Long id) {

        unitService.deleteUnit(id);

        return ResponseEntity.ok(
                "Unit deleted successfully");
    }
}