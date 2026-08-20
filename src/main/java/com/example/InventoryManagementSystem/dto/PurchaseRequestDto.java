package com.example.InventoryManagementSystem.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class PurchaseRequestDto {

    @NotNull(message = "supplierId is required")
    private Long supplierId;

    private String invoiceNumber;

    private BigDecimal totalAmount;

    private BigDecimal tax;

    private String paymentStatus;

    private Long createdBy;

    // Optional: when present and non-empty, createPurchase also creates a PurchaseItem row per
    // line and increments that product's stock. When omitted, only the purchase header is saved
    // (kept optional for direct API callers that manage items separately via /purchase-items).
    @Valid
    private List<PurchaseLineItemRequestDto> items;
}