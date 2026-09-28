package com.example.InventoryManagementSystem.controllor;

import com.example.InventoryManagementSystem.dto.LeaveRequestRequestDTO;
import com.example.InventoryManagementSystem.dto.LeaveRequestResponseDTO;
import com.example.InventoryManagementSystem.exception.AccessDeniedException;
import com.example.InventoryManagementSystem.service.CurrentUserService;
import com.example.InventoryManagementSystem.service.LeaveRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/leave-requests")
@RequiredArgsConstructor
public class LeaveRequestController {

    private final LeaveRequestService service;
    private final CurrentUserService currentUserService;

    // The logged-in user's own leave requests — user resolved from the JWT, never a query param.
    @GetMapping("/my")
    public ResponseEntity<List<LeaveRequestResponseDTO>> getMine() {
        Long userId = currentUserService.getCurrentUserId();
        if (userId == null) throw new AccessDeniedException("Not authenticated");
        return ResponseEntity.ok(service.getByUserId(userId));
    }

    @PostMapping
    public ResponseEntity<LeaveRequestResponseDTO> create(@Valid @RequestBody LeaveRequestRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<LeaveRequestResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<LeaveRequestResponseDTO>> getAll(@RequestParam(required = false) Long userId) {
        return ResponseEntity.ok(userId != null ? service.getByUserId(userId) : service.getAll());
    }

    @PutMapping("/{id}")
    public ResponseEntity<LeaveRequestResponseDTO> update(@PathVariable Long id, @RequestBody LeaveRequestRequestDTO dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<LeaveRequestResponseDTO> approve(@PathVariable Long id) {
        return ResponseEntity.ok(service.approve(id));
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<LeaveRequestResponseDTO> reject(@PathVariable Long id) {
        return ResponseEntity.ok(service.reject(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok("Leave request deleted successfully");
    }
}
