package com.example.InventoryManagementSystem.controllor;

import com.example.InventoryManagementSystem.dto.VehicleRequestDTO;
import com.example.InventoryManagementSystem.dto.VehicleResponseDTO;
import com.example.InventoryManagementSystem.service.VehicleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vehicles")
@RequiredArgsConstructor
public class VehicleController {

    private final VehicleService service;

    @PostMapping
    public ResponseEntity<VehicleResponseDTO> create(@Valid @RequestBody VehicleRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createVehicle(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<VehicleResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getVehicleById(id));
    }

    @GetMapping
    public ResponseEntity<List<VehicleResponseDTO>> getAll() {
        return ResponseEntity.ok(service.getAllVehicles());
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<VehicleResponseDTO>> getByCustomerId(@PathVariable Long customerId) {
        return ResponseEntity.ok(service.getVehiclesByCustomerId(customerId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<VehicleResponseDTO> update(@PathVariable Long id, @RequestBody VehicleRequestDTO dto) {
        return ResponseEntity.ok(service.updateVehicle(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        service.deleteVehicle(id);
        return ResponseEntity.ok("Vehicle deleted successfully");
    }
}
