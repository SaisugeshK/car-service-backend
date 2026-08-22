package com.example.InventoryManagementSystem.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Data
public class OfferResponseDTO {

    private Long offerId;
    private String offerName;
    private String description;
    private String discountType;
    private BigDecimal discountValue;
    private LocalDate startDate;
    private LocalDate endDate;
    private String vehicleType;
    private Long categoryId;
    private String categoryName;
    private BigDecimal minimumBillAmount;
    private String terms;
    private String status;
    private OffsetDateTime createdAt;
}
