package com.example.InventoryManagementSystem.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

// One row per status transition a job card actually went through — powers the visual timeline
// on the Job Card detail page with real dates instead of just "step N of M". Written once per
// change, never edited or deleted, so it doubles as a lightweight audit trail of the workflow.
@Entity
@Table(name = "job_card_status_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class JobCardStatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long jobCardStatusHistoryId;

    @Column(nullable = false)
    private Long jobCardId;

    @Column(nullable = false)
    private String status;

    private OffsetDateTime changedAt = OffsetDateTime.now();
}
