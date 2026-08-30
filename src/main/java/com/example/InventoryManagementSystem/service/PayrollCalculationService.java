package com.example.InventoryManagementSystem.service;

import com.example.InventoryManagementSystem.Repository.AttendanceRepository;
import com.example.InventoryManagementSystem.Repository.LeaveRequestRepository;
import com.example.InventoryManagementSystem.Repository.OvertimeEntryRepository;
import com.example.InventoryManagementSystem.model.Attendance;
import com.example.InventoryManagementSystem.model.EmployeeSalaryConfig;
import com.example.InventoryManagementSystem.model.LeaveRequest;
import com.example.InventoryManagementSystem.model.OvertimeEntry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.Map;

/**
 * HRM/payroll — the one formula engine both the scheduled job and the manual "Generate Now"
 * endpoint call (spec §10: "do not duplicate payroll formulas in multiple places"). A plain
 * concrete service, not an interface+impl pair like the rest of this codebase — it's internal
 * plumbing consumed only by PayrollServiceImpl, not something callers plug alternate
 * implementations into, so there's nothing an interface would add here.
 *
 * <p>V1 deduction formula (no prior rule existed anywhere in this app before this feature —
 * this is the documented assumption, isolated in one place so it can be swapped later without
 * hunting through the codebase):
 * <pre>
 *   perDayRate         = basicPay / workingDaysInMonth
 *   attendanceDeduction = perDayRate * absentDays
 *   leaveDeduction      = perDayRate * unpaidLeaveDays
 * </pre>
 * Paid leave costs nothing and is treated like a present day. A working day is any day that
 * isn't a Sunday (no holiday calendar exists in this app yet — see class-level assumption in
 * the HRM plan). Each working day in the period is resolved into exactly one bucket — PRESENT,
 * PAID_LEAVE, UNPAID_LEAVE, or ABSENT — so attendanceDeductions and leaveDeductions can never
 * double-count the same day (spec §8/§7 — "do not count unpaid leave twice").
 */
@Service
@RequiredArgsConstructor
class PayrollCalculationService {

    private final AttendanceRepository attendanceRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final OvertimeEntryRepository overtimeEntryRepository;

    private static final int SCALE = 2;

    PayrollComputation calculate(Long userId, EmployeeSalaryConfig config, int year, int month) {
        YearMonth period = YearMonth.of(year, month);
        LocalDate periodStart = period.atDay(1);
        LocalDate periodEnd = period.atEndOfMonth();

        int workingDays = 0;
        for (LocalDate d = periodStart; !d.isAfter(periodEnd); d = d.plusDays(1)) {
            if (d.getDayOfWeek() != DayOfWeek.SUNDAY) workingDays++;
        }

        // Day-by-day resolution: Leave takes precedence over Attendance for a given date, so a
        // day covered by both an (unlikely, but possible) attendance row and an approved leave is
        // never counted twice.
        Map<LocalDate, String> dayStatus = new HashMap<>();
        for (Attendance a : attendanceRepository.findByUserIdAndAttendanceDateBetween(userId, periodStart, periodEnd)) {
            if (a.getAttendanceDate().getDayOfWeek() != DayOfWeek.SUNDAY) {
                dayStatus.put(a.getAttendanceDate(), a.getStatus());
            }
        }
        for (LeaveRequest leave : leaveRequestRepository.findApprovedOverlapping(userId, periodStart, periodEnd)) {
            LocalDate from = leave.getStartDate().isBefore(periodStart) ? periodStart : leave.getStartDate();
            LocalDate to = leave.getEndDate().isAfter(periodEnd) ? periodEnd : leave.getEndDate();
            for (LocalDate d = from; !d.isAfter(to); d = d.plusDays(1)) {
                if (d.getDayOfWeek() != DayOfWeek.SUNDAY) {
                    dayStatus.put(d, "PAID".equals(leave.getLeaveType()) ? "PAID_LEAVE" : "UNPAID_LEAVE");
                }
            }
        }

        int presentDays = 0, absentDays = 0, paidLeaveDays = 0, unpaidLeaveDays = 0;
        for (LocalDate d = periodStart; !d.isAfter(periodEnd); d = d.plusDays(1)) {
            if (d.getDayOfWeek() == DayOfWeek.SUNDAY) continue;
            String status = dayStatus.getOrDefault(d, "ABSENT"); // unmarked working day defaults to ABSENT (documented assumption)
            switch (status) {
                case "PAID_LEAVE" -> paidLeaveDays++;
                case "UNPAID_LEAVE" -> unpaidLeaveDays++;
                case "PRESENT" -> presentDays++;
                default -> absentDays++; // ABSENT, or any unrecognized status
            }
        }
        // Paid leave counts as paid time worked, same as a present day.
        presentDays += paidLeaveDays;

        BigDecimal basicPay = nz(config.getBasicPay());
        BigDecimal perDayRate = workingDays > 0
                ? basicPay.divide(BigDecimal.valueOf(workingDays), 4, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        BigDecimal attendanceDeductions = perDayRate.multiply(BigDecimal.valueOf(absentDays)).setScale(SCALE, RoundingMode.HALF_UP);
        BigDecimal leaveDeductions = perDayRate.multiply(BigDecimal.valueOf(unpaidLeaveDays)).setScale(SCALE, RoundingMode.HALF_UP);

        BigDecimal overtimeAmount = overtimeEntryRepository
                .findByUserIdAndStatusAndWorkDateBetween(userId, "APPROVED", periodStart, periodEnd)
                .stream()
                .map(OvertimeEntry::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(SCALE, RoundingMode.HALF_UP);

        BigDecimal hra = nz(config.getHra());
        BigDecimal otherAllowances = nz(config.getOtherAllowances());
        BigDecimal flatDeductions = nz(config.getDeductions());

        BigDecimal grossPay = basicPay.add(hra).add(otherAllowances).add(overtimeAmount).setScale(SCALE, RoundingMode.HALF_UP);
        BigDecimal netPay = grossPay.subtract(flatDeductions).subtract(attendanceDeductions).subtract(leaveDeductions).setScale(SCALE, RoundingMode.HALF_UP);
        // Net pay never goes negative — a heavily-absent month reduces pay to zero, not into debt.
        if (netPay.compareTo(BigDecimal.ZERO) < 0) netPay = BigDecimal.ZERO;

        return new PayrollComputation(
                basicPay.setScale(SCALE, RoundingMode.HALF_UP),
                hra.setScale(SCALE, RoundingMode.HALF_UP),
                otherAllowances.setScale(SCALE, RoundingMode.HALF_UP),
                overtimeAmount,
                flatDeductions.setScale(SCALE, RoundingMode.HALF_UP),
                attendanceDeductions,
                leaveDeductions,
                grossPay,
                netPay,
                workingDays, presentDays, absentDays, paidLeaveDays, unpaidLeaveDays
        );
    }

    private static BigDecimal nz(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }
}
