package com.example.InventoryManagementSystem.dto;




import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class SalesRequestDTO {

    @NotNull(message = "customerId is required")
    private Long customerId;
    private Long createdBy;

    private String invoiceNumber;
    private String paymentStatus;
    private String paymentMethod;

    private String vehicleModel;
    private String registrationNumber;
    private Integer odometerReading;

    private BigDecimal totalAmount;

    private BigDecimal discountAmount = BigDecimal.ZERO;
    private BigDecimal paidAmount = BigDecimal.ZERO;

    // Optional: when present and non-empty, createSale runs the full billing cascade (stock
    // check/decrement, tax, computed totals). When omitted, createSale falls back to a plain
    // header-only save using totalAmount/paymentStatus as supplied — preserves the generic
    // Sales CRUD screen (src/pages/Sales.jsx), which manages sale headers directly with no
    // line items of its own.
    @Valid
    private List<SalesLineItemRequestDTO> items;
}
