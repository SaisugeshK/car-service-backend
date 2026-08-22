package com.example.InventoryManagementSystem.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

// One row per "Launch Campaign" click — a snapshot of when the offer was announced and how many
// customers existed at that moment. Per-recipient send status lives in NotificationLog
// (referenceType=OFFER_CAMPAIGN, referenceId=this row's id), reusing the same honest
// NOT_CONFIGURED/SENT/DELIVERED/FAILED semantics as every other notification in this app —
// never a fabricated "delivered".
@Entity
@Table(name = "offer_campaigns")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OfferCampaign {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long offerCampaignId;

    @Column(nullable = false)
    private Long offerId;

    // Snapshot at launch time — every customer on file when the campaign went out, regardless
    // of whether they were eligible for this specific offer.
    private Integer totalCustomers;

    private OffsetDateTime launchedAt = OffsetDateTime.now();
}
