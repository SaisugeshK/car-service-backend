package com.example.InventoryManagementSystem.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

// The invoice-completion payload POS submits. There is no manual/header-only path for Invoice
// (unlike the legacy Sales module) — every invoice is created with real line items.
@Data
public class InvoiceRequestDTO {

    @NotNull(message = "customerId is required")
    private Long customerId;

    private Long vehicleId;
    private Integer odometerReading; // snapshotted onto the invoice; also updates Vehicle.odometer
    private Long counterId;

    // Request-only — never persisted on the Invoice itself. When present, the server validates
    // every line against that job card's approved estimate (or, if the estimate was rejected,
    // that the only line is the inspection fee) before the invoice is created. Omitted by POS and
    // the manual Invoices page, which are unaffected by this check.
    private Long jobCardId;

    private String paymentMethod;
    private String paymentStatus; // optional — derived from paidAmount/grandTotal if omitted

    private BigDecimal paidAmount = BigDecimal.ZERO;
    // Request-only — when true, paidAmount is ignored and the invoice is recorded as paid in
    // full at the server-computed grand total (GST included), so a caller that only knows
    // "the customer paid" never has to reproduce the tax math to get the amount exactly right.
    private boolean payInFull;
    private BigDecimal discountAmount = BigDecimal.ZERO; // additional/overall discount (manual)

    // Optional offer coupon. The server validates it and computes its discount itself (added on
    // top of discountAmount) — the client never gets to say how much a coupon is worth.
    private String couponCode;

    private Integer createdBy;

    @NotEmpty(message = "At least one service or product line is required")
    @Valid
    private List<InvoiceLineItemRequestDTO> items;
}
