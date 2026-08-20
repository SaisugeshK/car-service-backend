package com.example.InventoryManagementSystem.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HoldInvoiceResponseDto {

    private Long holdId;
    private Long customerId;
    private String customerName;
    private Long vehicleId;
    private String vehicleModel;
    private String registrationNumber;
    private String status;
    private String data;
    private OffsetDateTime createdAt;
}
