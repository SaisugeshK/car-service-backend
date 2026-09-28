package com.example.InventoryManagementSystem.controllor;

import com.example.InventoryManagementSystem.dto.VisitLookupDTO;
import com.example.InventoryManagementSystem.dto.VisitRequestDTO;
import com.example.InventoryManagementSystem.dto.VisitResponseDTO;
import com.example.InventoryManagementSystem.service.VisitService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Logging and reading visits is open to SUPER_ADMIN and EMPLOYEE (SecurityConfig); deleting a
// visit is SUPER_ADMIN only. VisitService scopes the full list to the visits an EMPLOYEE handled.
@RestController
@RequestMapping("/api/visits")
@RequiredArgsConstructor
public class VisitController {

    private final VisitService service;

    @GetMapping
    public ResponseEntity<List<VisitResponseDTO>> list() {
        return ResponseEntity.ok(service.list());
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<VisitResponseDTO>> forCustomer(@PathVariable Long customerId) {
        return ResponseEntity.ok(service.forCustomer(customerId));
    }

    // Live preview for the Log Visit form — who/what a mobile + registration resolves to.
    @GetMapping("/lookup")
    public ResponseEntity<VisitLookupDTO> lookup(@RequestParam(required = false) String phone,
                                                 @RequestParam(required = false) String registrationNumber) {
        return ResponseEntity.ok(service.lookup(phone, registrationNumber));
    }

    @PostMapping
    public ResponseEntity<VisitResponseDTO> log(@Valid @RequestBody VisitRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.log(dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok("Visit deleted successfully");
    }
}
