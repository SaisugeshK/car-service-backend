package com.example.InventoryManagementSystem.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ServiceMasterRequestDTO {

    private String serviceCode;

    @NotBlank(message = "Service name is required")
    private String serviceName;

    private String description;
    private BigDecimal defaultPrice;
    private BigDecimal gstPercentage;
    private Integer durationMinutes;
    private String vehicleType;
    private String status;
}
