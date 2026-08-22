package com.example.InventoryManagementSystem.controllor;

import com.example.InventoryManagementSystem.dto.OfferCampaignResponseDTO;
import com.example.InventoryManagementSystem.dto.OfferLaunchRequestDTO;
import com.example.InventoryManagementSystem.dto.OfferRequestDTO;
import com.example.InventoryManagementSystem.dto.OfferResponseDTO;
import com.example.InventoryManagementSystem.service.OfferService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/offers")
@RequiredArgsConstructor
public class OfferController {

    private final OfferService service;

    @PostMapping
    public ResponseEntity<OfferResponseDTO> create(@Valid @RequestBody OfferRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<OfferResponseDTO> update(@PathVariable Long id, @RequestBody OfferRequestDTO dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @GetMapping
    public ResponseEntity<List<OfferResponseDTO>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<OfferResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok("Offer deleted successfully");
    }

    @PostMapping("/{id}/launch")
    public ResponseEntity<OfferCampaignResponseDTO> launch(@PathVariable Long id, @RequestBody(required = false) OfferLaunchRequestDTO dto) {
        return ResponseEntity.ok(service.launch(id, dto));
    }

    @GetMapping("/{id}/campaigns")
    public ResponseEntity<List<OfferCampaignResponseDTO>> getCampaigns(@PathVariable Long id) {
        return ResponseEntity.ok(service.getCampaigns(id));
    }
}
