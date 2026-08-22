package com.example.InventoryManagementSystem.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReviewRequestDTO {

    @NotNull(message = "customerId is required")
    private Long customerId;

    private Long vehicleId;
    private Long jobCardId;
    private Long invoiceId;

    @NotNull(message = "rating is required")
    @Min(value = 1, message = "rating must be between 1 and 5")
    @Max(value = 5, message = "rating must be between 1 and 5")
    private Integer rating;

    @Min(value = 1, message = "serviceQualityRating must be between 1 and 5")
    @Max(value = 5, message = "serviceQualityRating must be between 1 and 5")
    private Integer serviceQualityRating;

    @Min(value = 1, message = "staffBehaviorRating must be between 1 and 5")
    @Max(value = 5, message = "staffBehaviorRating must be between 1 and 5")
    private Integer staffBehaviorRating;

    @Min(value = 1, message = "serviceTimeRating must be between 1 and 5")
    @Max(value = 5, message = "serviceTimeRating must be between 1 and 5")
    private Integer serviceTimeRating;

    @Min(value = 1, message = "priceSatisfactionRating must be between 1 and 5")
    @Max(value = 5, message = "priceSatisfactionRating must be between 1 and 5")
    private Integer priceSatisfactionRating;

    private String comment;
}
