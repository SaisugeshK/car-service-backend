package com.example.InventoryManagementSystem.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "offers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Offer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long offerId;

    @Column(nullable = false)
    private String offerName;

    // Auto-generated from the offer name on create (see OfferServiceImpl.generateCouponCode) and
    // kept stable afterwards — customers may already have been sent it.
    @Column(name = "coupon_code", unique = true, length = 60)
    private String couponCode;

    @Column(columnDefinition = "TEXT")
    private String description;

    // PERCENTAGE / FIXED_AMOUNT
    @Column(nullable = false)
    private String discountType;

    @Column(nullable = false)
    private BigDecimal discountValue;

    // When the offer becomes usable / stops being usable. Null = no bound on that side.
    @Column(name = "start_date_time")
    private OffsetDateTime startDateTime;

    @Column(name = "end_date_time")
    private OffsetDateTime endDateTime;

    // Legacy date-only columns from before start/end carried a time. Only read by
    // OfferBackfillRunner to seed start_date_time/end_date_time on existing rows — never written.
    @Column(name = "start_date", insertable = false, updatable = false)
    private LocalDate legacyStartDate;

    @Column(name = "end_date", insertable = false, updatable = false)
    private LocalDate legacyEndDate;

    // CAR / BIKE / null = both, same convention as Vehicle/Product/ServiceMaster.
    private String vehicleType;

    // Optional label (Service/Category on the Offers form) — not used to gate a bill.
    private Long categoryId;

    private BigDecimal minimumBillAmount;

    // Optional cap on how many (non-cancelled) invoices may use this offer. Null = unlimited.
    @Column(name = "usage_limit")
    private Integer usageLimit;

    @Column(columnDefinition = "TEXT")
    private String terms;

    // Manual on/off switch — ACTIVE / INACTIVE. Upcoming/expired is derived from the dates at
    // the moment of use (OfferServiceImpl.lifecycleStatus), so no scheduler has to flip it.
    private String status = "ACTIVE";

    private OffsetDateTime createdAt = OffsetDateTime.now();
}
