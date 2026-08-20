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

    private String paymentMethod;
    private String paymentStatus; // optional — derived from paidAmount/grandTotal if omitted

    private BigDecimal paidAmount = BigDecimal.ZERO;
    private BigDecimal discountAmount = BigDecimal.ZERO; // additional/overall discount

    private Integer createdBy;

    @NotEmpty(message = "At least one service or product line is required")
    @Valid
    private List<InvoiceLineItemRequestDTO> items;
}
