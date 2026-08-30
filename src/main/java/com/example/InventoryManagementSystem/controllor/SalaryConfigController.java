package com.example.InventoryManagementSystem.controllor;

import com.example.InventoryManagementSystem.dto.EmployeeSalaryConfigRequestDTO;
import com.example.InventoryManagementSystem.dto.EmployeeSalaryConfigResponseDTO;
import com.example.InventoryManagementSystem.service.EmployeeSalaryConfigService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/salary-configs")
@RequiredArgsConstructor
public class SalaryConfigController {

    private final EmployeeSalaryConfigService service;

    @PostMapping
    public ResponseEntity<EmployeeSalaryConfigResponseDTO> create(@Valid @RequestBody EmployeeSalaryConfigRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmployeeSalaryConfigResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<EmployeeSalaryConfigResponseDTO> getByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(service.getByUserId(userId));
    }

    @GetMapping
    public ResponseEntity<List<EmployeeSalaryConfigResponseDTO>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @PutMapping("/{id}")
    public ResponseEntity<EmployeeSalaryConfigResponseDTO> update(@PathVariable Long id, @RequestBody EmployeeSalaryConfigRequestDTO dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    // No physical delete — deactivate instead (spec §22), keeping history/audit intact.
    @DeleteMapping("/{id}")
    public ResponseEntity<EmployeeSalaryConfigResponseDTO> deactivate(@PathVariable Long id) {
        return ResponseEntity.ok(service.deactivate(id));
    }
}
