package com.example.InventoryManagementSystem.service;

import com.example.InventoryManagementSystem.Repository.EmployeeSalaryConfigRepository;
import com.example.InventoryManagementSystem.Repository.RoleRepository;
import com.example.InventoryManagementSystem.Repository.SalaryPaymentRepository;
import com.example.InventoryManagementSystem.Repository.UserRepository;
import com.example.InventoryManagementSystem.dto.CancelPayrollRequestDTO;
import com.example.InventoryManagementSystem.dto.MarkSalaryPaidRequestDTO;
import com.example.InventoryManagementSystem.dto.PayrollGenerationResultDTO;
import com.example.InventoryManagementSystem.dto.SalaryPaymentResponseDTO;
import com.example.InventoryManagementSystem.exception.AccessDeniedException;
import com.example.InventoryManagementSystem.exception.ResourceNotFoundException;
import com.example.InventoryManagementSystem.model.EmployeeSalaryConfig;
import com.example.InventoryManagementSystem.model.Role;
import com.example.InventoryManagementSystem.model.SalaryPayment;
import com.example.InventoryManagementSystem.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PayrollServiceImpl implements PayrollService {

    private static final Set<String> METHODS_REQUIRING_REFERENCE = Set.of("BANK_TRANSFER", "UPI", "CHEQUE");
    private static final Set<String> VALID_PAYMENT_METHODS = Set.of("CASH", "BANK_TRANSFER", "UPI", "CHEQUE", "OTHER");

    private final SalaryPaymentRepository salaryPaymentRepository;
    private final EmployeeSalaryConfigRepository salaryConfigRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PayrollRowGenerator payrollRowGenerator;
    private final CurrentUserService currentUserService;
    private final AuditLogService auditLogService;

    @Value("${app.timezone:Asia/Kolkata}")
    private String appTimezone;

    // ---- Manual trigger (spec §20) ----
    // Deliberately NOT @Transactional here — see PayrollRowGenerator's class comment. Each
    // employee's insert now runs in its own transaction; wrapping the whole batch in one shared
    // transaction was what let one employee's constraint-violation race poison every other
    // employee's already-successful work in the same call (found via concurrency testing).
    @Override
    public PayrollGenerationResultDTO generate(Integer year, Integer month, Long userId) {
        LocalDate period = defaultPeriod();
        int y = year != null ? year : period.getYear();
        int m = month != null ? month : period.getMonthValue();
        return runGeneration(y, m, userId, "MANUAL", currentUserService.getCurrentUserId());
    }

    // ---- Scheduled trigger (spec §18) — 00:00 on the 1st of every month, previous month's
    // payroll (pay-in-arrears). Same runGeneration(...) the manual endpoint calls — one formula,
    // one generation path, per spec §10/§20.
    @Scheduled(cron = "0 0 0 1 * *", zone = "${app.timezone:Asia/Kolkata}")
    public void runMonthlyPayroll() {
        LocalDate period = defaultPeriod();
        PayrollGenerationResultDTO result = runGeneration(period.getYear(), period.getMonthValue(), null, "SCHEDULED", null);
        log.info("Scheduled payroll run for {}-{}: {} employees found, {} generated, {} already existed, {} failed.",
                result.getPayPeriodYear(), result.getPayPeriodMonth(), result.getEmployeesFound(),
                result.getGeneratedCount(), result.getAlreadyExistedCount(), result.getFailedCount());
        if (result.getFailedCount() > 0) {
            log.warn("Scheduled payroll failures: {}", result.getFailures());
        }
    }

    private LocalDate defaultPeriod() {
        return LocalDate.now(ZoneId.of(appTimezone)).minusMonths(1);
    }

    private PayrollGenerationResultDTO runGeneration(int year, int month, Long userId, String source, Long triggeredByUserId) {
        PayrollGenerationResultDTO result = new PayrollGenerationResultDTO();
        result.setPayPeriodYear(year);
        result.setPayPeriodMonth(month);

        List<EmployeeSalaryConfig> configs;
        if (userId != null) {
            configs = salaryConfigRepository.findByUserId(userId)
                    .filter(EmployeeSalaryConfig::getActive)
                    .map(List::of)
                    .orElseGet(List::of);
        } else {
            configs = salaryConfigRepository.findByActiveTrue();
        }
        result.setEmployeesFound(configs.size());

        for (EmployeeSalaryConfig config : configs) {
            try {
                // Eligibility (spec §6): active salary config AND active user. An inactive user is
                // simply not processed — not an error, so it isn't counted as a failure.
                User user = userRepository.findById(config.getUserId()).orElse(null);
                if (user == null || !Boolean.TRUE.equals(user.getActive())) continue;

                // Each employee's insert is its own transaction (PayrollRowGenerator) — one
                // employee's DB-constraint race can no longer roll back everyone else's already-
                // committed work in the same batch (spec §19; see the class comment on
                // PayrollRowGenerator for the concurrency bug this replaced).
                PayrollRowGenerator.Outcome outcome;
                try {
                    outcome = payrollRowGenerator.generateOne(config, user.getUserId(), year, month, source, triggeredByUserId);
                } catch (DataIntegrityViolationException raceLost) {
                    // Lost the race against a concurrent request generating this exact employee/
                    // period between our exists-check and our insert (see PayrollRowGenerator's
                    // class comment) — the payroll got generated, just by the other request. Not
                    // a failure; caught here, one level above the now-already-rolled-back
                    // REQUIRES_NEW transaction, rather than inside it.
                    outcome = PayrollRowGenerator.Outcome.ALREADY_EXISTS;
                }
                if (outcome == PayrollRowGenerator.Outcome.ALREADY_EXISTS) {
                    result.setAlreadyExistedCount(result.getAlreadyExistedCount() + 1);
                } else {
                    result.setGeneratedCount(result.getGeneratedCount() + 1);
                }
            } catch (Exception ex) {
                // One employee's failure must never abort the whole batch (spec §19) — now truly
                // isolated per-employee at the transaction level too, not just at the Java
                // try/catch level.
                result.setFailedCount(result.getFailedCount() + 1);
                result.getFailures().add("user #" + config.getUserId() + ": " + ex.getMessage());
                log.error("Payroll generation failed for user #{} ({}-{}): {}", config.getUserId(), year, month, ex.getMessage(), ex);
            }
        }

        auditLogService.record("PAYROLL_GENERATED", "SALARY_PAYMENT", null,
                String.format("Payroll generated for %d-%02d (%s): %d found, %d generated, %d already existed, %d failed.",
                        year, month, source, result.getEmployeesFound(), result.getGeneratedCount(),
                        result.getAlreadyExistedCount(), result.getFailedCount()));

        return result;
    }

    // ---- Queries ----
    @Override
    public List<SalaryPaymentResponseDTO> getAll(Integer year, Integer month, Long userId, String status) {
        return salaryPaymentRepository.findAll().stream()
                .filter(p -> year == null || year.equals(p.getPayPeriodYear()))
                .filter(p -> month == null || month.equals(p.getPayPeriodMonth()))
                .filter(p -> userId == null || userId.equals(p.getUserId()))
                .filter(p -> status == null || status.equalsIgnoreCase(p.getStatus()))
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public SalaryPaymentResponseDTO getById(Long id) {
        SalaryPayment payment = salaryPaymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payroll record not found with id: " + id));
        enforceOwnershipIfEmployee(payment);
        return mapToDto(payment);
    }

    @Override
    public List<SalaryPaymentResponseDTO> getMyPayslips() {
        Long currentUserId = currentUserService.getCurrentUserId();
        if (currentUserId == null) {
            throw new AccessDeniedException("Not authenticated");
        }
        return salaryPaymentRepository.findByUserId(currentUserId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    // The concrete fix for spec §25 — an EMPLOYEE fetching a payroll row that isn't their own
    // gets 403, never a peek at someone else's salary just because they guessed/incremented an id.
    private void enforceOwnershipIfEmployee(SalaryPayment payment) {
        User current = currentUserService.getCurrentUser();
        String roleName = currentRoleName(current);
        if ("EMPLOYEE".equals(roleName) && (current == null || !Objects.equals(current.getUserId(), payment.getUserId()))) {
            throw new AccessDeniedException("You do not have access to this payroll record");
        }
    }

    private String currentRoleName(User user) {
        if (user == null || user.getRoleId() == null) return null;
        return roleRepository.findById(user.getRoleId()).map(Role::getRoleName).orElse(null);
    }

    // ---- Mark paid / cancel (spec §15/§16/§17) ----
    @Override
    public SalaryPaymentResponseDTO markPaid(Long id, MarkSalaryPaidRequestDTO dto) {
        SalaryPayment payment = salaryPaymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payroll record not found with id: " + id));
        if (!"PENDING".equals(payment.getStatus())) {
            throw new IllegalArgumentException("Only a PENDING payroll record can be marked paid (current status: " + payment.getStatus() + ")");
        }
        String method = dto.getPaymentMethod() != null ? dto.getPaymentMethod().toUpperCase() : null;
        if (method == null || !VALID_PAYMENT_METHODS.contains(method)) {
            throw new IllegalArgumentException("Invalid payment method: " + dto.getPaymentMethod());
        }
        if (METHODS_REQUIRING_REFERENCE.contains(method) && (dto.getPaymentReference() == null || dto.getPaymentReference().isBlank())) {
            throw new IllegalArgumentException(method + " payments require a payment reference");
        }

        payment.setStatus("PAID");
        payment.setPaidAt(OffsetDateTime.now());
        payment.setPaidByUserId(currentUserService.getCurrentUserId());
        payment.setPaymentMethod(method);
        payment.setPaymentReference(dto.getPaymentReference());
        SalaryPayment saved;
        try {
            // @Version on SalaryPayment (added after concurrency testing found two simultaneous
            // mark-paid calls both succeeding) makes this throw if someone else already updated
            // this exact row since we loaded it above.
            saved = salaryPaymentRepository.save(payment);
        } catch (org.springframework.orm.ObjectOptimisticLockingFailureException lostRace) {
            throw new IllegalArgumentException("This payroll record was just updated by someone else — reload and try again");
        }

        auditLogService.record("PAYROLL_MARKED_PAID", "SALARY_PAYMENT", saved.getSalaryPaymentId(),
                "Payroll " + saved.getPaymentNumber() + " for user #" + saved.getUserId() + " marked PAID via " + method + ".");
        return mapToDto(saved);
    }

    @Override
    public SalaryPaymentResponseDTO cancel(Long id, CancelPayrollRequestDTO dto) {
        SalaryPayment payment = salaryPaymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payroll record not found with id: " + id));
        // PAID -> CANCELLED is explicitly forbidden (spec §15/§16) — paid payroll is historical
        // financial data, corrected by other means, never cancelled after the fact.
        if (!"PENDING".equals(payment.getStatus())) {
            throw new IllegalArgumentException("Only a PENDING payroll record can be cancelled (current status: " + payment.getStatus() + ")");
        }

        payment.setStatus("CANCELLED");
        payment.setCancelledAt(OffsetDateTime.now());
        payment.setCancelledByUserId(currentUserService.getCurrentUserId());
        payment.setCancellationReason(dto.getCancellationReason());
        SalaryPayment saved;
        try {
            saved = salaryPaymentRepository.save(payment);
        } catch (org.springframework.orm.ObjectOptimisticLockingFailureException lostRace) {
            throw new IllegalArgumentException("This payroll record was just updated by someone else — reload and try again");
        }

        auditLogService.record("PAYROLL_CANCELLED", "SALARY_PAYMENT", saved.getSalaryPaymentId(),
                "Payroll " + saved.getPaymentNumber() + " for user #" + saved.getUserId() + " cancelled: " + dto.getCancellationReason());
        return mapToDto(saved);
    }

    private SalaryPaymentResponseDTO mapToDto(SalaryPayment p) {
        SalaryPaymentResponseDTO dto = new SalaryPaymentResponseDTO();
        dto.setSalaryPaymentId(p.getSalaryPaymentId());
        dto.setPaymentNumber(p.getPaymentNumber());
        dto.setUserId(p.getUserId());
        dto.setSalaryConfigId(p.getSalaryConfigId());
        dto.setBasicPay(p.getBasicPay());
        dto.setHra(p.getHra());
        dto.setOtherAllowances(p.getOtherAllowances());
        dto.setOvertimeAmount(p.getOvertimeAmount());
        dto.setDeductions(p.getDeductions());
        dto.setAttendanceDeductions(p.getAttendanceDeductions());
        dto.setLeaveDeductions(p.getLeaveDeductions());
        dto.setGrossPay(p.getGrossPay());
        dto.setNetPay(p.getNetPay());
        dto.setWorkingDays(p.getWorkingDays());
        dto.setPresentDays(p.getPresentDays());
        dto.setAbsentDays(p.getAbsentDays());
        dto.setPaidLeaveDays(p.getPaidLeaveDays());
        dto.setUnpaidLeaveDays(p.getUnpaidLeaveDays());
        dto.setPayPeriodMonth(p.getPayPeriodMonth());
        dto.setPayPeriodYear(p.getPayPeriodYear());
        dto.setStatus(p.getStatus());
        dto.setGeneratedAt(p.getGeneratedAt());
        dto.setGeneratedBy(p.getGeneratedBy());
        dto.setGenerationSource(p.getGenerationSource());
        dto.setPaidAt(p.getPaidAt());
        dto.setPaidByUserId(p.getPaidByUserId());
        dto.setPaymentMethod(p.getPaymentMethod());
        dto.setPaymentReference(p.getPaymentReference());
        dto.setCancelledAt(p.getCancelledAt());
        dto.setCancelledByUserId(p.getCancelledByUserId());
        dto.setCancellationReason(p.getCancellationReason());
        dto.setNotes(p.getNotes());

        userRepository.findById(p.getUserId()).ifPresent(u -> {
            dto.setUserName(displayName(u));
            if (u.getRoleId() != null) {
                roleRepository.findById(u.getRoleId()).map(Role::getRoleName).ifPresent(dto::setRoleName);
            }
        });
        if (p.getGeneratedBy() != null) {
            userRepository.findById(p.getGeneratedBy()).ifPresent(u -> dto.setGeneratedByName(displayName(u)));
        }
        if (p.getPaidByUserId() != null) {
            userRepository.findById(p.getPaidByUserId()).ifPresent(u -> dto.setPaidByName(displayName(u)));
        }
        return dto;
    }

    private String displayName(User user) {
        if (user.getFullName() != null && !user.getFullName().isBlank()) return user.getFullName();
        return user.getUsername();
    }
}
