package com.example.InventoryManagementSystem.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class NotificationSendRequestDTO {

    @NotBlank(message = "channel is required (WHATSAPP or SMS)")
    private String channel;

    private String recipientPhone;
    private String referenceType;
    private Long referenceId;
    private String subject;
    private String message;
}
