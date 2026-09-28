package com.example.InventoryManagementSystem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
public class OfferRequestDTO {

    @NotBlank(message = "Offer name is required")
    private String offerName;

    private String description;

    @NotBlank(message = "discountType is required (PERCENTAGE or FIXED_AMOUNT)")
    private String discountType;

    @NotNull(message = "discountValue is required")
    @Positive(message = "discountValue must be greater than zero")
    private BigDecimal discountValue;

    private OffsetDateTime startDateTime;
    private OffsetDateTime endDateTime;
    private String vehicleType;
    private Long categoryId;

    @PositiveOrZero(message = "minimumBillAmount cannot be negative")
    private BigDecimal minimumBillAmount;

    @Positive(message = "usageLimit must be at least 1")
    private Integer usageLimit;

    private String terms;
    private String status;
}
