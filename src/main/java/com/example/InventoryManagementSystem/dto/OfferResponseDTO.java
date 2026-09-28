package com.example.InventoryManagementSystem.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
public class OfferResponseDTO {

    private Long offerId;
    private String offerName;
    private String couponCode;
    private String description;
    private String discountType;
    private BigDecimal discountValue;
    private OffsetDateTime startDateTime;
    private OffsetDateTime endDateTime;
    private String vehicleType;
    private Long categoryId;
    private String categoryName;
    private BigDecimal minimumBillAmount;
    private Integer usageLimit;
    private long usedCount;
    private String terms;
    // Manual ACTIVE / INACTIVE switch.
    private String status;
    // Derived right now: INACTIVE / UPCOMING / ACTIVE / EXPIRED / LIMIT_REACHED.
    private String lifecycleStatus;
    // ACTIVE and ending within the next 24 hours.
    private boolean endingSoon;
    private OffsetDateTime createdAt;
}
