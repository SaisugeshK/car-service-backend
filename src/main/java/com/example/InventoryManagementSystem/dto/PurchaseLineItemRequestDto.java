package com.example.InventoryManagementSystem.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class PurchaseLineItemRequestDto {

    @NotNull(message = "productId is required")
    private Long productId;

    @NotNull(message = "quantity is required")
    @Positive(message = "quantity must be greater than zero")
    private Integer quantity;

    private BigDecimal purchasePrice = BigDecimal.ZERO;

    private BigDecimal taxAmount = BigDecimal.ZERO;
}
