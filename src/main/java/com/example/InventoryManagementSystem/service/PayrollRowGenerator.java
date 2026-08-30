package com.example.InventoryManagementSystem.service;

import com.example.InventoryManagementSystem.Repository.SalaryPaymentRepository;
import com.example.InventoryManagementSystem.model.EmployeeSalaryConfig;
import com.example.InventoryManagementSystem.model.SalaryPayment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

/**
 * Pre-deployment fix — discovered via concurrency testing: two truly-simultaneous "Generate
 * Payroll" calls for the same period both used to run inside ONE shared @Transactional spanning
 * every employee in the batch. When both requests' pre-insert existence checks raced and passed
 * for the same employee, the DB's unique constraint on (user_id, month, year) correctly rejected
 * the second INSERT — but that constraint violation poisoned the *entire* surrounding transaction
 * (Spring/Hibernate marks it rollback-only), which meant every other employee successfully
 * processed earlier in that same request got silently rolled back too, and the request itself
 * failed with a confusing "Transaction silently rolled back" 500/400 instead of a clean result.
 * That directly defeats spec §19 ("one employee's failure must never stop the batch").
 *
 * The fix: each employee's generation is its own REQUIRES_NEW transaction via this dedicated
 * bean (a separate bean is required — Spring AOP can't intercept a same-class self-invocation of
 * a @Transactional method, so this can't just be a private method on PayrollServiceImpl). A lost
 * race now fails (and rolls back) only that one employee's own transaction; every other employee
 * in the batch, and the request as a whole, is unaffected.
 */
@Service
@RequiredArgsConstructor
class PayrollRowGenerator {

    enum Outcome { GENERATED, ALREADY_EXISTS }

    private final SalaryPaymentRepository salaryPaymentRepository;
    private final PayrollCalculationService payrollCalculationService;
    private final SettingsLookupService settingsLookupService;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    Outcome generateOne(EmployeeSalaryConfig config, Long userId, int year, int month, String source, Long triggeredByUserId) {
        if (salaryPaymentRepository.existsByUserIdAndPayPeriodMonthAndPayPeriodYear(userId, month, year)) {
            return Outcome.ALREADY_EXISTS;
        }

        PayrollComputation calc = payrollCalculationService.calculate(userId, config, year, month);

        SalaryPayment payment = SalaryPayment.builder()
                .userId(userId)
                .salaryConfigId(config.getSalaryConfigId())
                .basicPay(calc.getBasicPay())
                .hra(calc.getHra())
                .otherAllowances(calc.getOtherAllowances())
                .overtimeAmount(calc.getOvertimeAmount())
                .deductions(calc.getDeductions())
                .attendanceDeductions(calc.getAttendanceDeductions())
                .leaveDeductions(calc.getLeaveDeductions())
                .grossPay(calc.getGrossPay())
                .netPay(calc.getNetPay())
                .workingDays(calc.getWorkingDays())
                .presentDays(calc.getPresentDays())
                .absentDays(calc.getAbsentDays())
                .paidLeaveDays(calc.getPaidLeaveDays())
                .unpaidLeaveDays(calc.getUnpaidLeaveDays())
                .payPeriodMonth(month)
                .payPeriodYear(year)
                .status("PENDING")
                .generatedAt(OffsetDateTime.now())
                .generatedBy(triggeredByUserId)
                .generationSource(source)
                .build();

        // Deliberately not caught here — a flush-time constraint violation leaves Hibernate's
        // session for *this* transaction unusable regardless of whether the exception is caught
        // in Java, so the only safe move is to let it propagate and cleanly roll back this one
        // REQUIRES_NEW transaction (harmless: nothing else was written in it). The caller
        // (PayrollServiceImpl) catches DataIntegrityViolationException across this method-call
        // boundary instead, once this transaction has already finished rolling back, and treats
        // it as "someone else generated this first" rather than a real failure.
        SalaryPayment saved = salaryPaymentRepository.saveAndFlush(payment);
        saved.setPaymentNumber(settingsLookupService.get("salary_payment_prefix", "SAL") + "-" + saved.getSalaryPaymentId());
        salaryPaymentRepository.save(saved);
        return Outcome.GENERATED;
    }
}
