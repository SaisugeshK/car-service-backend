package com.example.InventoryManagementSystem.service;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

// HRM/payroll — internal result of PayrollCalculationService.calculate(...). Not exposed over
// the API directly; PayrollServiceImpl copies these fields onto the SalaryPayment snapshot.
// Package-private on purpose — this is plumbing between the two payroll services, not a response
// shape any controller should ever return as-is.
@Data
@AllArgsConstructor
class PayrollComputation {

    private BigDecimal basicPay;
    private BigDecimal hra;
    private BigDecimal otherAllowances;
    private BigDecimal overtimeAmount;
    private BigDecimal deductions;
    private BigDecimal attendanceDeductions;
    private BigDecimal leaveDeductions;
    private BigDecimal grossPay;
    private BigDecimal netPay;

    private int workingDays;
    private int presentDays;
    private int absentDays;
    private int paidLeaveDays;
    private int unpaidLeaveDays;
}
