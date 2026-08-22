package com.example.InventoryManagementSystem.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

// Named ServiceMaster (not "Service") to avoid colliding with org.springframework.stereotype.Service
// and this codebase's XxxServiceImpl naming convention for the service layer. This is the car-service
// catalog (labour items like "General Car Service", "Wheel Alignment") — distinct from Product, and
// never stock-tracked.
@Entity
@Table(name = "service_master")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ServiceMaster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long serviceId;

    @Column(unique = true)
    private String serviceCode;

    @Column(nullable = false)
    private String serviceName;

    @Column(columnDefinition = "TEXT")
    private String description;

    private BigDecimal defaultPrice;

    private BigDecimal gstPercentage = BigDecimal.ZERO;

    private Integer durationMinutes;

    // CAR / BIKE / BOTH — null is treated as BOTH (applies to every vehicle) so existing rows
    // created before this field stay visible everywhere instead of silently disappearing.
    private String vehicleType;

    private String status = "active";

    private OffsetDateTime createdAt = OffsetDateTime.now();
    private OffsetDateTime updatedAt = OffsetDateTime.now();

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }
}
