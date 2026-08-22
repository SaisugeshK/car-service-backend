package com.example.InventoryManagementSystem.dto;

import lombok.Data;

import java.time.OffsetDateTime;

// Stats are computed fresh from the real NotificationLog rows this campaign created — never
// stored/faked. notConfigured is broken out separately from failed/sent because that's the
// honest state every send resolves to right now (no WhatsApp/SMS provider integrated yet) —
// folding it into "Failed" would misleadingly imply a real send attempt was rejected, and
// folding it into "Sent" would be an outright lie.
@Data
public class OfferCampaignResponseDTO {

    private Long offerCampaignId;
    private Long offerId;
    private OffsetDateTime launchedAt;

    private Integer totalCustomers;
    private Integer eligible;
    private Integer sent;
    private Integer delivered;
    private Integer failed;
    private Integer notConfigured;
    private Integer pending;
}
