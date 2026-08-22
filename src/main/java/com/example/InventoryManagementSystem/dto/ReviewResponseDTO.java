package com.example.InventoryManagementSystem.dto;

import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class ReviewResponseDTO {

    private Long reviewId;
    private Long customerId;
    private String customerName;
    private Long vehicleId;
    private String vehicleModel;
    private String registrationNumber;
    private Long jobCardId;
    private String jobCardNumber;
    private Long invoiceId;
    private String invoiceNumber;

    private Integer rating;
    private Integer serviceQualityRating;
    private Integer staffBehaviorRating;
    private Integer serviceTimeRating;
    private Integer priceSatisfactionRating;

    private String comment;
    private OffsetDateTime createdAt;
}
