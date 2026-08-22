package com.example.InventoryManagementSystem.dto;

import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class NotificationLogResponseDTO {

    private Long notificationLogId;
    private String channel;
    private String recipientPhone;
    private String referenceType;
    private Long referenceId;
    private String subject;
    private String message;
    private String status;
    private String errorMessage;
    private OffsetDateTime createdAt;
}
