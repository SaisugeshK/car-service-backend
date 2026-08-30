package com.example.InventoryManagementSystem.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Data
public class ServiceMasterResponseDTO {

    private Long serviceId;
    private String serviceCode;
    private String serviceName;
    private String description;
    private BigDecimal defaultPrice;
    private BigDecimal gstPercentage;
    private Integer durationMinutes;
    private String vehicleType;
    private String status;
    private List<ServiceSizePriceDTO> sizePrices;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
