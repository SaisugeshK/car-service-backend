package com.example.InventoryManagementSystem.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
public class SalaryPaymentResponseDTO {

    private Long salaryPaymentId;
    private String paymentNumber;
    private Long userId;
    private String userName;
    private String roleName;
    private Long salaryConfigId;

    private BigDecimal basicPay;
    private BigDecimal hra;
    private BigDecimal otherAllowances;
    private BigDecimal overtimeAmount;
    private BigDecimal deductions;
    private BigDecimal attendanceDeductions;
    private BigDecimal leaveDeductions;
    private BigDecimal grossPay;
    private BigDecimal netPay;

    private Integer workingDays;
    private Integer presentDays;
    private Integer absentDays;
    private Integer paidLeaveDays;
    private Integer unpaidLeaveDays;

    private Integer payPeriodMonth;
    private Integer payPeriodYear;

    private String status;

    private OffsetDateTime generatedAt;
    private Long generatedBy;
    private String generatedByName;
    private String generationSource;

    private OffsetDateTime paidAt;
    private Long paidByUserId;
    private String paidByName;
    private String paymentMethod;
    private String paymentReference;

    private OffsetDateTime cancelledAt;
    private Long cancelledByUserId;
    private String cancellationReason;

    private String notes;
}
