package com.example.InventoryManagementSystem.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

// One review per completed job card (see ReviewServiceImpl — enforced, not just convention).
// `rating` is the overall 1-5 score; the four category ratings are optional finer-grained
// feedback the customer may or may not have given.
@Entity
@Table(name = "reviews")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reviewId;

    @Column(nullable = false)
    private Long customerId;

    private Long vehicleId;
    private Long jobCardId;
    private Long invoiceId;

    @Column(nullable = false)
    private Integer rating; // 1-5, overall

    private Integer serviceQualityRating;   // 1-5
    private Integer staffBehaviorRating;    // 1-5
    private Integer serviceTimeRating;      // 1-5
    private Integer priceSatisfactionRating; // 1-5

    @Column(columnDefinition = "TEXT")
    private String comment;

    private OffsetDateTime createdAt = OffsetDateTime.now();
}
