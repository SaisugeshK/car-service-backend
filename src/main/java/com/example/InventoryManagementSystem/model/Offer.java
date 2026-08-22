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

    @Column(columnDefinition = "TEXT")
    private String description;

    // PERCENTAGE / FIXED_AMOUNT
    @Column(nullable = false)
    private String discountType;

    @Column(nullable = false)
    private BigDecimal discountValue;

    private LocalDate startDate;
    private LocalDate endDate;

    // CAR / BIKE / null = both, same convention as Vehicle/Product/ServiceMaster.
    private String vehicleType;

    private Long categoryId;

    private BigDecimal minimumBillAmount;

    @Column(columnDefinition = "TEXT")
    private String terms;

    private String status = "ACTIVE"; // ACTIVE / INACTIVE

    private OffsetDateTime createdAt = OffsetDateTime.now();
}
