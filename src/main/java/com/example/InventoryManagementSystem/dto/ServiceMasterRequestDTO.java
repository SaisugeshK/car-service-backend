package com.example.InventoryManagementSystem.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

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

    // Explicit price per vehicle size band. Sizes not listed here fall back to defaultPrice.
    @Valid
    private List<ServiceSizePriceDTO> sizePrices;
}
