package com.example.InventoryManagementSystem.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

// Phase 33/34 — closing the flagged gap: axios.js's refresh-token flow was already fully coded
// and wired, dormant only because no backend ever issued one. Long-lived (30 days) alongside the
// existing 24h access token — renewing a session no longer means a full re-login. Rotated on every
// use (old row revoked, new one issued) and revoked on logout, so a captured token has a real
// expiry window instead of working forever.
@Entity
@Table(name = "refresh_tokens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long refreshTokenId;

    @Column(nullable = false, unique = true, length = 512)
    private String token;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private OffsetDateTime expiresAt;

    @Column(nullable = false)
    @Builder.Default
    private Boolean revoked = false;

    private OffsetDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = OffsetDateTime.now();
    }
}
