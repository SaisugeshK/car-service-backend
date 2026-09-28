package com.example.InventoryManagementSystem.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

// One offer checked against a specific bill: whether it can be applied, and if so how much it
// takes off; if not, the exact reason (same messages the server enforces at invoice time).
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OfferEligibilityDTO {
    private OfferResponseDTO offer;
    private boolean eligible;
    private String message;
    private BigDecimal discountAmount;
}
