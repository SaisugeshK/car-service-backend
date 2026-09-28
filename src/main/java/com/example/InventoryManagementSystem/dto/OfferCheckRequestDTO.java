package com.example.InventoryManagementSystem.dto;

import lombok.Data;

import java.math.BigDecimal;

// The bill an offer is being checked against. billAmount = total before any offer/manual
// discount (GST included) — the same base InvoiceServiceImpl uses when it redeems the coupon.
@Data
public class OfferCheckRequestDTO {
    private String couponCode; // only for validate-coupon
    private BigDecimal billAmount;
    private String vehicleType;   // CAR / BIKE / null
}
