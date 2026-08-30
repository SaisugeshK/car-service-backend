package com.example.InventoryManagementSystem.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "vehicles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long vehicleId;

    @Column(nullable = false)
    private Long customerId;

    private String make;

    @Column(nullable = false)
    private String vehicleModel;

    private String variant;

    private String registrationNumber;

    // Latest known odometer reading (km) — kept current after each completed invoice; the
    // reading at time of a specific visit is snapshotted onto that invoice separately.
    private Integer odometer;

    private String vehicleType; // body style, e.g. Hatchback / Sedan / SUV — free text, unrelated to vehicleCategory
    private String fuelType;    // e.g. Petrol / Diesel / CNG / Electric
    private String color;
    private Integer year;

    private String chassisNumber; // VIN
    private String engineNumber;

    // CAR or BIKE — the top-level split the whole service workflow (inspection, estimate, job
    // card, billing, reports) filters on. Kept separate from vehicleType (body style) above.
    private String vehicleCategory;

    // Size band code — SMALL/SEDAN/SUV/MUV/LUXURY for a car, STANDARD/PREMIUM for a bike. Used to
    // pick the service's price for this vehicle (service_prices); null → the service's base price.
    private String sizeClass;

    private String insuranceCompany;
    private java.time.LocalDate insuranceExpiry;
    private java.time.LocalDate pucExpiry;

    @Column(columnDefinition = "TEXT")
    private String notes;

    private OffsetDateTime createdAt = OffsetDateTime.now();
    private OffsetDateTime updatedAt = OffsetDateTime.now();

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }
}
