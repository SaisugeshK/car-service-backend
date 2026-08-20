package com.example.InventoryManagementSystem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class InvoiceLineItemRequestDTO {

    @NotBlank(message = "itemType is required (SERVICE or PRODUCT)")
    private String itemType;

    private Long productId; // required when itemType = PRODUCT
    private Long serviceId; // required when itemType = SERVICE

    private String description;

    @NotNull(message = "quantity is required")
    @Positive(message = "quantity must be greater than zero")
    private BigDecimal quantity;

    // Optional override — defaults to the Product's sellingPrice / ServiceMaster's defaultPrice.
    private BigDecimal unitPrice;

    private BigDecimal discount = BigDecimal.ZERO;
}
