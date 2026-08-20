package com.example.InventoryManagementSystem.controllor;

import com.example.InventoryManagementSystem.dto.ServiceMasterRequestDTO;
import com.example.InventoryManagementSystem.dto.ServiceMasterResponseDTO;
import com.example.InventoryManagementSystem.service.ServiceMasterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/services")
@RequiredArgsConstructor
public class ServiceMasterController {

    private final ServiceMasterService service;

    @PostMapping
    public ResponseEntity<ServiceMasterResponseDTO> create(@Valid @RequestBody ServiceMasterRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createService(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServiceMasterResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getServiceById(id));
    }

    @GetMapping
    public ResponseEntity<List<ServiceMasterResponseDTO>> getAll() {
        return ResponseEntity.ok(service.getAllServices());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ServiceMasterResponseDTO> update(@PathVariable Long id, @RequestBody ServiceMasterRequestDTO dto) {
        return ResponseEntity.ok(service.updateService(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        service.deleteService(id);
        return ResponseEntity.ok("Service deleted successfully");
    }
}
