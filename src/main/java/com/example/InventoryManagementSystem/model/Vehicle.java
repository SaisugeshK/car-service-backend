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

    private String vehicleType; // e.g. Hatchback / Sedan / SUV
    private String fuelType;    // e.g. Petrol / Diesel / CNG / Electric
    private String color;
    private Integer year;

    private String chassisNumber; // VIN
    private String engineNumber;

    @Column(columnDefinition = "TEXT")
    private String notes;

    private OffsetDateTime createdAt = OffsetDateTime.now();
    private OffsetDateTime updatedAt = OffsetDateTime.now();

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }
}
