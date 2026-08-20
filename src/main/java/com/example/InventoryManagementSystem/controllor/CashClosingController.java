package com.example.InventoryManagementSystem.controllor;

import com.example.InventoryManagementSystem.dto.CashClosingRequestDto;
import com.example.InventoryManagementSystem.dto.CashClosingResponseDto;
import com.example.InventoryManagementSystem.service.CashClosingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cash-closing")
@RequiredArgsConstructor
public class CashClosingController {

    private final CashClosingService cashClosingService;

    // CREATE
    @PostMapping
    public ResponseEntity<CashClosingResponseDto>
    createCashClosing(
            @RequestBody CashClosingRequestDto dto) {

        return ResponseEntity.ok(
                cashClosingService
                        .createCashClosing(dto));
    }

    // GET ALL
    @GetMapping
    public ResponseEntity<List<CashClosingResponseDto>>
    getAllCashClosings() {

        return ResponseEntity.ok(
                cashClosingService
                        .getAllCashClosings());
    }

    // GET BY ID
    @GetMapping("/{id}")
    public ResponseEntity<CashClosingResponseDto>
    getCashClosingById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                cashClosingService
                        .getCashClosingById(id));
    }

    // UPDATE
    @PutMapping("/{id}")
    public ResponseEntity<CashClosingResponseDto>
    updateCashClosing(
            @PathVariable Long id,
            @RequestBody CashClosingRequestDto dto) {

        return ResponseEntity.ok(
                cashClosingService
                        .updateCashClosing(id, dto));
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<String>
    deleteCashClosing(
            @PathVariable Long id) {

        cashClosingService
                .deleteCashClosing(id);

        return ResponseEntity.ok(
                "Cash closing deleted successfully");
    }
}