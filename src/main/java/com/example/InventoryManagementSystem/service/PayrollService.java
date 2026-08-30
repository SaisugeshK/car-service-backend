package com.example.InventoryManagementSystem.service;

import com.example.InventoryManagementSystem.dto.CancelPayrollRequestDTO;
import com.example.InventoryManagementSystem.dto.MarkSalaryPaidRequestDTO;
import com.example.InventoryManagementSystem.dto.PayrollGenerationResultDTO;
import com.example.InventoryManagementSystem.dto.SalaryPaymentResponseDTO;

import java.util.List;

public interface PayrollService {

    // Manual "Generate Now" (spec §20/§14) — year/month default to the previous calendar month
    // when omitted, matching the scheduler's own pay-in-arrears period. userId narrows generation
    // to one employee (catch-up), otherwise every active employee is processed. Idempotent either
    // way: an employee/period combination that already has a SalaryPayment row is skipped, never
    // duplicated.
    PayrollGenerationResultDTO generate(Integer year, Integer month, Long userId);

    List<SalaryPaymentResponseDTO> getAll(Integer year, Integer month, Long userId, String status);

    // Ownership-checked: a SUPER_ADMIN/MANAGER may fetch any row; an EMPLOYEE may fetch only a
    // row whose userId is their own, else an AccessDeniedException (-> 403) is thrown (spec §25).
    SalaryPaymentResponseDTO getById(Long id);

    // The authenticated caller's own payroll history — userId is always resolved from the JWT via
    // CurrentUserService, never trusted from the client (spec §24).
    List<SalaryPaymentResponseDTO> getMyPayslips();

    SalaryPaymentResponseDTO markPaid(Long id, MarkSalaryPaidRequestDTO dto);

    SalaryPaymentResponseDTO cancel(Long id, CancelPayrollRequestDTO dto);
}
