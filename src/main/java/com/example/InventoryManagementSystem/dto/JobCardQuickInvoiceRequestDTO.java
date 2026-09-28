package com.example.InventoryManagementSystem.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

// The single-page job card form's "Paid" submission: complaint/technician/delivery date plus the
// billed lines, discount and payment method, all in one call. Replaces the multi-step
// inspection -> estimate -> approval -> QC flow for job cards billed from that form.
@Data
public class JobCardQuickInvoiceRequestDTO {

    private String complaint;
    private OffsetDateTime expectedDelivery;
    private Long technicianUserId;

    // Offer coupon — validated and priced by the server (InvoiceServiceImpl), stored on the invoice.
    private String couponCode;

    // Extra manual discount, on top of the coupon's.
    @PositiveOrZero(message = "discountAmount cannot be negative")
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @NotBlank(message = "paymentMethod is required")
    private String paymentMethod;

    private Long counterId;

    @NotEmpty(message = "At least one service or product line is required")
    @Valid
    private List<InvoiceLineItemRequestDTO> items;
}
