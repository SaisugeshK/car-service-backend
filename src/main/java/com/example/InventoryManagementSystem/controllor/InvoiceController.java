package com.example.InventoryManagementSystem.controllor;

import com.example.InventoryManagementSystem.dto.InvoiceRequestDTO;
import com.example.InventoryManagementSystem.dto.InvoiceResponseDTO;
import com.example.InventoryManagementSystem.service.InvoiceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/invoices")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceService service;

    @PostMapping
    public ResponseEntity<InvoiceResponseDTO> createInvoice(@Valid @RequestBody InvoiceRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createInvoice(dto));
    }

    @GetMapping
    public ResponseEntity<List<InvoiceResponseDTO>> getAllInvoices() {
        return ResponseEntity.ok(service.getAllInvoices());
    }

    @GetMapping("/{id}")
    public ResponseEntity<InvoiceResponseDTO> getInvoiceById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getInvoiceById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<InvoiceResponseDTO> updateInvoice(@PathVariable Long id, @RequestBody InvoiceRequestDTO dto) {
        return ResponseEntity.ok(service.updateInvoice(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteInvoice(@PathVariable Long id) {
        service.deleteInvoice(id);
        return ResponseEntity.ok("Invoice deleted successfully");
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<InvoiceResponseDTO> cancelInvoice(@PathVariable Long id) {
        return ResponseEntity.ok(service.cancelInvoice(id));
    }
}
