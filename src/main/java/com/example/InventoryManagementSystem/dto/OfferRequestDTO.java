package com.example.InventoryManagementSystem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class OfferRequestDTO {

    @NotBlank(message = "Offer name is required")
    private String offerName;

    private String description;

    @NotBlank(message = "discountType is required (PERCENTAGE or FIXED_AMOUNT)")
    private String discountType;

    @NotNull(message = "discountValue is required")
    private BigDecimal discountValue;

    private LocalDate startDate;
    private LocalDate endDate;
    private String vehicleType;
    private Long categoryId;
    private BigDecimal minimumBillAmount;
    private String terms;
    private String status;
}
