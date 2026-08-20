package com.example.InventoryManagementSystem.controllor;

import com.example.InventoryManagementSystem.dto.CustomerFollowUpRequestDTO;
import com.example.InventoryManagementSystem.dto.CustomerFollowUpResponseDTO;
import com.example.InventoryManagementSystem.service.CustomerFollowUpService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/follow-ups")
@RequiredArgsConstructor
public class CustomerFollowUpController {

    private final CustomerFollowUpService service;

    @PostMapping
    public ResponseEntity<CustomerFollowUpResponseDTO> create(@Valid @RequestBody CustomerFollowUpRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createFollowUp(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerFollowUpResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getFollowUpById(id));
    }

    @GetMapping
    public ResponseEntity<List<CustomerFollowUpResponseDTO>> getAll() {
        return ResponseEntity.ok(service.getAllFollowUps());
    }

    @PutMapping("/{id}")
    public ResponseEntity<CustomerFollowUpResponseDTO> update(@PathVariable Long id, @RequestBody CustomerFollowUpRequestDTO dto) {
        return ResponseEntity.ok(service.updateFollowUp(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        service.deleteFollowUp(id);
        return ResponseEntity.ok("Follow-up deleted successfully");
    }
}
