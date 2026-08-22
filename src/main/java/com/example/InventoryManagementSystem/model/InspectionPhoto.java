package com.example.InventoryManagementSystem.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

// Phase 33/34 — closes the flagged gap: JobCardDetail.jsx's InspectionTab already had a Photos
// section wired up to nothing, explicitly disabled with "Not configured" rather than faking an
// upload. storedFileName is the on-disk name (a generated UUID, collision-proof, never the raw
// user-supplied filename); originalFileName is kept only for display.
@Entity
@Table(name = "inspection_photos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InspectionPhoto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long inspectionPhotoId;

    @Column(nullable = false)
    private Long jobCardId;

    // Same taxonomy as InspectionItem.category (Engine / Battery / Brakes / ...).
    @Column(nullable = false)
    private String category;

    @Column(nullable = false)
    private String storedFileName;

    private String originalFileName;

    private String contentType;

    private Long fileSize;

    private OffsetDateTime uploadedAt;

    @PrePersist
    public void prePersist() {
        this.uploadedAt = OffsetDateTime.now();
    }
}
