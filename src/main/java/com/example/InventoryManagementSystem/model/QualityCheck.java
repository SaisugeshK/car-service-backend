package com.example.InventoryManagementSystem.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "quality_checks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QualityCheck {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long qualityCheckId;

    @Column(nullable = false)
    private Long jobCardId;

    // Free-form JSON checklist (engine/brakes/lights/AC/tyres/road test/cleaning/tools removed/
    // old parts returned/complaint resolved -> true/false), same opaque-blob pattern already used
    // for HoldInvoice.data — the frontend owns the shape, backend just stores/returns it.
    @Column(columnDefinition = "TEXT")
    private String checklistJson;

    // PASS / FAIL
    private String result;

    @Column(columnDefinition = "TEXT")
    private String notes;

    private Long checkedBy;

    private OffsetDateTime checkedAt = OffsetDateTime.now();

    private OffsetDateTime createdAt = OffsetDateTime.now();
}
