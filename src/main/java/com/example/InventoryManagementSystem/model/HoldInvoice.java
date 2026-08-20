package com.example.InventoryManagementSystem.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "hold_invoices")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HoldInvoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "hold_id")
    private Long holdId;

    @Column(name = "customer_id")
    private Long customerId;

    @Column(name = "vehicle_id")
    private Long vehicleId;

    // Denormalized so the "Held Bills" list doesn't need to parse the JSON blob just to render.
    @Column(name = "status", length = 20)
    @Builder.Default
    private String status = "HELD";

    // Structured JSON snapshot of the POS cart (customer, vehicle, line items, discounts,
    // payment method) — opaque to the backend, serialized/deserialized entirely by the frontend.
    @Column(columnDefinition = "TEXT")
    private String data;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = OffsetDateTime.now();
    }
}
