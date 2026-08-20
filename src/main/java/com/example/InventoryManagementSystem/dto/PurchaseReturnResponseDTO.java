package com.example.InventoryManagementSystem.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseReturnResponseDTO {

    private Integer purchaseReturnId;

    private Integer purchaseId;

    private Integer supplierId;

    private LocalDateTime returnDate;

    private BigDecimal totalAmount;

    private String notes;
}