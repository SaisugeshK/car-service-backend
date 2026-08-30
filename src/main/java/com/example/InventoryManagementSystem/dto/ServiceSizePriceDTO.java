package com.example.InventoryManagementSystem.dto;

import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ServiceSizePriceDTO {

    private String sizeClassCode;

    @PositiveOrZero(message = "price cannot be negative")
    private BigDecimal price;
}
