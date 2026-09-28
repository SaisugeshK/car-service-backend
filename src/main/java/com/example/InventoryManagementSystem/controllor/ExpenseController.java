package com.example.InventoryManagementSystem.controllor;

import com.example.InventoryManagementSystem.dto.ExpenseRequestDTO;
import com.example.InventoryManagementSystem.dto.ExpenseResponseDTO;
import com.example.InventoryManagementSystem.service.ExpenseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

// Open to SUPER_ADMIN and EMPLOYEE (SecurityConfig); ExpenseService limits an EMPLOYEE to the
// expenses they created.
@RestController
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService service;

    @GetMapping
    public ResponseEntity<List<ExpenseResponseDTO>> list() {
        return ResponseEntity.ok(service.list());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExpenseResponseDTO> get(@PathVariable Long id) {
        return ResponseEntity.ok(service.get(id));
    }

    @PostMapping
    public ResponseEntity<ExpenseResponseDTO> create(@Valid @RequestBody ExpenseRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ExpenseResponseDTO> update(@PathVariable Long id, @Valid @RequestBody ExpenseRequestDTO dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok("Expense deleted successfully");
    }

    @PostMapping(value = "/{id}/receipt", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ExpenseResponseDTO> uploadReceipt(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(service.uploadReceipt(id, file));
    }

    @GetMapping("/{id}/receipt")
    public ResponseEntity<byte[]> getReceipt(@PathVariable Long id) {
        ExpenseService.StoredReceipt receipt = service.loadReceipt(id);
        return ResponseEntity.ok()
                .contentType(receipt.contentType() != null ? MediaType.parseMediaType(receipt.contentType()) : MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + (receipt.fileName() != null ? receipt.fileName().replace("\"", "") : "receipt") + "\"")
                .body(receipt.data());
    }

    @DeleteMapping("/{id}/receipt")
    public ResponseEntity<ExpenseResponseDTO> removeReceipt(@PathVariable Long id) {
        return ResponseEntity.ok(service.removeReceipt(id));
    }
}
