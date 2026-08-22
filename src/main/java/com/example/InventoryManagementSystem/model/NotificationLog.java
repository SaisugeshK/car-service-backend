package com.example.InventoryManagementSystem.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

// An honest record of every WhatsApp/SMS send attempt — created whether or not a provider is
// actually configured. No WhatsApp/SMS provider is integrated into this backend yet (see the
// Phase 5 audit note), so status is currently always NOT_CONFIGURED or FAILED; SENT/DELIVERED
// are real states reserved for once a provider (Twilio/Gupshup/etc.) is wired into
// NotificationServiceImpl — this entity and API never fake that it happened before then.
@Entity
@Table(name = "notification_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NotificationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long notificationLogId;

    // WHATSAPP / SMS
    @Column(nullable = false)
    private String channel;

    private String recipientPhone;

    // ESTIMATE / INVOICE / REMINDER / OFFER / ...
    private String referenceType;
    private Long referenceId;

    private String subject;

    @Column(columnDefinition = "TEXT")
    private String message;

    // NOT_CONFIGURED / SENT / DELIVERED / FAILED
    @Column(nullable = false)
    private String status;

    private String errorMessage;

    private OffsetDateTime createdAt = OffsetDateTime.now();
}
