package com.example.InventoryManagementSystem.dto;

import lombok.Data;

@Data
public class OfferLaunchRequestDTO {
    // WHATSAPP or SMS — defaults to WHATSAPP if omitted.
    private String channel;
}
