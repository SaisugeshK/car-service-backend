package com.example.InventoryManagementSystem.controllor;

import com.example.InventoryManagementSystem.dto.InvoiceResponseDTO;
import com.example.InventoryManagementSystem.dto.JobCardRequestDTO;
import com.example.InventoryManagementSystem.dto.JobCardResponseDTO;
import com.example.InventoryManagementSystem.service.JobCardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/job-cards")
@RequiredArgsConstructor
public class JobCardController {

    private final JobCardService service;

    @PostMapping
    public ResponseEntity<JobCardResponseDTO> create(@Valid @RequestBody JobCardRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createJobCard(dto));
    }

    @PostMapping("/from-appointment/{appointmentId}")
    public ResponseEntity<JobCardResponseDTO> createFromAppointment(@PathVariable Long appointmentId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createFromAppointment(appointmentId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<JobCardResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getJobCardById(id));
    }

    @GetMapping
    public ResponseEntity<List<JobCardResponseDTO>> getAll() {
        return ResponseEntity.ok(service.getAllJobCards());
    }

    @PutMapping("/{id}")
    public ResponseEntity<JobCardResponseDTO> update(@PathVariable Long id, @RequestBody JobCardRequestDTO dto) {
        return ResponseEntity.ok(service.updateJobCard(id, dto));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<JobCardResponseDTO> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(service.updateStatus(id, body.get("status")));
    }

    @PostMapping("/{id}/generate-invoice")
    public ResponseEntity<InvoiceResponseDTO> generateInvoice(@PathVariable Long id, @RequestBody(required = false) Map<String, Object> body) {
        String paymentMethod = body != null && body.get("paymentMethod") != null ? body.get("paymentMethod").toString() : null;
        BigDecimal paidAmount = body != null && body.get("paidAmount") != null
                ? new BigDecimal(body.get("paidAmount").toString()) : BigDecimal.ZERO;
        Long counterId = body != null && body.get("counterId") != null
                ? Long.valueOf(body.get("counterId").toString()) : null;
        return ResponseEntity.ok(service.generateInvoice(id, paymentMethod, paidAmount, counterId));
    }

    @PostMapping("/{id}/deliver")
    public ResponseEntity<JobCardResponseDTO> markDelivered(@PathVariable Long id) {
        return ResponseEntity.ok(service.markDelivered(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        service.deleteJobCard(id);
        return ResponseEntity.ok("Job card deleted successfully");
    }
}
