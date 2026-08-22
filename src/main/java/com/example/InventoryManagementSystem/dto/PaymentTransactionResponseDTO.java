package com.example.InventoryManagementSystem.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@Builder
public class PaymentTransactionResponseDTO {
    private Long transactionId;
    private Long invoiceId;
    private String invoiceNumber;
    private Long customerId;
    private String customerName;
    private String paymentMethod;
    private String transactionReference;
    private BigDecimal amount;
    private OffsetDateTime paymentDate;
    private Long receivedByUserId;
    private String receivedByName;
    private String notes;
}