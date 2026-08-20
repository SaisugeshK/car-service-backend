package com.example.InventoryManagementSystem.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "inspection_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InspectionItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long inspectionItemId;

    @Column(nullable = false)
    private Long jobCardId;

    // Engine / Battery / Brakes / Tyres / Suspension / AC / Lights / Electrical / Fluids /
    // Exterior / Interior
    @Column(nullable = false)
    private String category;

    // GOOD / ATTENTION / URGENT / NOT_CHECKED
    private String status = "NOT_CHECKED";

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(columnDefinition = "TEXT")
    private String recommendation;

    private OffsetDateTime createdAt = OffsetDateTime.now();
    private OffsetDateTime updatedAt = OffsetDateTime.now();

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }
}
