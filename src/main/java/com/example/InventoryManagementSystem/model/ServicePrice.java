package com.example.InventoryManagementSystem.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

// An explicit price for one ServiceMaster at one vehicle size band (e.g. "General Service" for a
// SUV = 3000). When a size has no row here the estimate falls back to ServiceMaster.defaultPrice.
@Entity
@Table(name = "service_prices",
        uniqueConstraints = @UniqueConstraint(columnNames = {"service_id", "size_class_code"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ServicePrice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long serviceId;

    // SMALL / SEDAN / SUV / MUV / LUXURY (car) or STANDARD / PREMIUM (bike)
    @Column(nullable = false)
    private String sizeClassCode;

    @Column(nullable = false)
    private BigDecimal price;
}
