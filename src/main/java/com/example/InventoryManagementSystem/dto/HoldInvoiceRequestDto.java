package com.example.InventoryManagementSystem.dto;

import lombok.Data;

@Data
public class HoldInvoiceRequestDto {

    private Long customerId;
    private Long vehicleId;
    private String status;
    private String data;
}
