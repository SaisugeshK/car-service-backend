package com.example.InventoryManagementSystem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class EstimateLineItemRequestDTO {

    @NotBlank(message = "itemType is required (SERVICE or PRODUCT)")
    private String itemType;

    private Long productId;
    private Long serviceId;
    private String description;

    @NotNull(message = "quantity is required")
    @Positive(message = "quantity must be greater than zero")
    private BigDecimal quantity;

    // Optional override — defaults to the Product's sellingPrice / ServiceMaster's defaultPrice.
    @PositiveOrZero(message = "unitPrice cannot be negative")
    private BigDecimal unitPrice;

    @PositiveOrZero(message = "discount cannot be negative")
    private BigDecimal discount = BigDecimal.ZERO;

    // CUSTOMER_REQUESTED / RECOMMENDED — defaults to RECOMMENDED server-side if left blank.
    private String workCategory;
}
