package com.example.InventoryManagementSystem.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CashClosingResponseDto {

    private Long closingId;

    private Long counterId;

    private String counterName;

    private BigDecimal openingCash;

    private BigDecimal closingCash;

    private BigDecimal totalSales;

    private OffsetDateTime createdAt;
}