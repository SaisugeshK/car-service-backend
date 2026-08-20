package com.example.InventoryManagementSystem.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "cash_closing")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CashClosing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "closing_id")
    private Long closingId;

    @ManyToOne
    @JoinColumn(name = "counter_id")
    private BillingCounter billingCounter;

    @Column(name = "opening_cash")
    private BigDecimal openingCash;

    @Column(name = "closing_cash")
    private BigDecimal closingCash;

    @Column(name = "total_sales")
    private BigDecimal totalSales;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = OffsetDateTime.now();
    }
}