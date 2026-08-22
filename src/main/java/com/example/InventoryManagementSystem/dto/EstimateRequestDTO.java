package com.example.InventoryManagementSystem.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class EstimateRequestDTO {

    @NotNull(message = "jobCardId is required")
    private Long jobCardId;

    @NotNull(message = "customerId is required")
    private Long customerId;

    private LocalDate validUntil;
    private BigDecimal discountAmount = BigDecimal.ZERO;
    private String notes;

    @NotEmpty(message = "At least one service or product line is required")
    @Valid
    private List<EstimateLineItemRequestDTO> items;
}
