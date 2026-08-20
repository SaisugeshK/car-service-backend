package com.example.InventoryManagementSystem.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CashClosingRequestDto {

    private Long counterId;

    private BigDecimal openingCash;

    private BigDecimal closingCash;

    private BigDecimal totalSales;
}