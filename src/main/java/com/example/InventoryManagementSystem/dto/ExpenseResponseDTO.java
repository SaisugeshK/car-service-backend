package com.example.InventoryManagementSystem.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Data
public class ExpenseResponseDTO {
    private Long expenseId;
    private String title;
    private String category;
    private String description;
    private BigDecimal amount;
    private String paymentMethod;
    private LocalDate expenseDate;
    private boolean hasReceipt;
    private String receiptFileName;
    private String receiptContentType;
    private Long createdByUserId;
    private String createdByName;
    private String createdByRole;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
