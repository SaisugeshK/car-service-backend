package com.example.InventoryManagementSystem.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

// Phase 30 — a permanent, never-edited compliance trail of who did what to which record and
// when. Distinct from NotificationEvent (Phase 29): that's an ephemeral, dismissible staff inbox;
// this is a record nobody marks read and nothing ever deletes. username is denormalized (copied
// at write time) so the trail still reads correctly even if the user account is later removed.
@Entity
@Table(name = "audit_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long auditLogId;

    private Long userId;

    @Column(nullable = false)
    private String username;

    // ESTIMATE_CREATED / ESTIMATE_REVISED / ESTIMATE_APPROVED / ESTIMATE_REJECTED /
    // ADDITIONAL_WORK_REQUESTED / ADDITIONAL_WORK_APPROVED / ADDITIONAL_WORK_REJECTED /
    // INVOICE_GENERATED / PAYMENT_RECEIVED / JOB_STATUS_CHANGED / DELIVERY_COMPLETED
    @Column(nullable = false)
    private String action;

    // ESTIMATE / ADDITIONAL_WORK / INVOICE / PAYMENT / JOB_CARD
    @Column(nullable = false)
    private String entityType;

    private Long entityId;

    @Column(columnDefinition = "TEXT")
    private String description;

    private OffsetDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = OffsetDateTime.now();
    }
}
