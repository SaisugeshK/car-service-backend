package com.example.InventoryManagementSystem.controllor;

import com.example.InventoryManagementSystem.dto.CancelPayrollRequestDTO;
import com.example.InventoryManagementSystem.dto.MarkSalaryPaidRequestDTO;
import com.example.InventoryManagementSystem.dto.PayrollGenerationResultDTO;
import com.example.InventoryManagementSystem.dto.SalaryPaymentResponseDTO;
import com.example.InventoryManagementSystem.service.PayrollService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payroll")
@RequiredArgsConstructor
public class PayrollController {

    private final PayrollService service;

    // Manual/catch-up generation (spec §20) — year/month default to the previous calendar month
    // when omitted (pay-in-arrears, same period the scheduler would use); userId narrows to one
    // employee. SecurityConfig restricts this whole path to SUPER_ADMIN/MANAGER already.
    @PostMapping("/generate")
    public ResponseEntity<PayrollGenerationResultDTO> generate(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Long userId) {
        return ResponseEntity.ok(service.generate(year, month, userId));
    }

    @GetMapping
    public ResponseEntity<List<SalaryPaymentResponseDTO>> getAll(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(service.getAll(year, month, userId, status));
    }

    // An EMPLOYEE's own payslip history — userId is resolved server-side from the JWT, never
    // trusted from a query param (spec §24).
    @GetMapping("/my")
    public ResponseEntity<List<SalaryPaymentResponseDTO>> getMyPayslips() {
        return ResponseEntity.ok(service.getMyPayslips());
    }

    // Ownership-checked in the service layer — an EMPLOYEE fetching a row that isn't theirs gets
    // 403, not the record (spec §25).
    @GetMapping("/{id}")
    public ResponseEntity<SalaryPaymentResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @PutMapping("/{id}/mark-paid")
    public ResponseEntity<SalaryPaymentResponseDTO> markPaid(@PathVariable Long id, @Valid @RequestBody MarkSalaryPaidRequestDTO dto) {
        return ResponseEntity.ok(service.markPaid(id, dto));
    }

    // PENDING -> CANCELLED only — a PAID record can never be cancelled (spec §15/§16), and there
    // is deliberately no DELETE endpoint on this resource: payroll history is never physically
    // removed.
    @PutMapping("/{id}/cancel")
    public ResponseEntity<SalaryPaymentResponseDTO> cancel(@PathVariable Long id, @Valid @RequestBody CancelPayrollRequestDTO dto) {
        return ResponseEntity.ok(service.cancel(id, dto));
    }
}
