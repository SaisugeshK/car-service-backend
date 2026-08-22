package com.example.InventoryManagementSystem.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

// Phase 29 — an in-app staff notification ("a new job card arrived", "estimate approved",
// "stock is low"), distinct from NotificationLog (an outbound WhatsApp/SMS attempt to a
// *customer*). This is a shared inbox for whoever is logged in — this app has no per-user
// notification targeting, so isRead is a single shared flag, not per-user; whoever reads it
// first marks it read for everyone. That's a deliberate v1 scope choice, not an oversight.
@Entity
@Table(name = "notification_events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long notificationEventId;

    // NEW_CUSTOMER / NEW_JOB / PENDING_ESTIMATE / ESTIMATE_APPROVED / ESTIMATE_REJECTED /
    // ADDITIONAL_APPROVAL / PAYMENT / READY_FOR_DELIVERY / REVIEW / REMINDER / LOW_STOCK /
    // OFFER_CAMPAIGN_RESULT
    @Column(nullable = false)
    private String type;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String message;

    // CUSTOMER / JOB_CARD / ESTIMATE / ADDITIONAL_WORK / PAYMENT / REVIEW / SERVICE_REMINDER /
    // PRODUCT / OFFER — lets the frontend deep-link the notification to the right screen.
    private String referenceType;
    private Long referenceId;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isRead = false;

    private OffsetDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = OffsetDateTime.now();
    }
}
