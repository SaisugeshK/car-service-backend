package com.example.InventoryManagementSystem.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseReturnItemRequestDTO {

    private Integer purchaseReturnId;

    private Integer productId;

    private Integer quantity;

    private BigDecimal price;
}