package com.example.InventoryManagementSystem.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "sales")
@Getter
@Setter
public class Sales {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long saleId;

    private Long customerId;
    private Long createdBy;

    private String invoiceNumber;
    private String paymentStatus;
    private String paymentMethod;

    // Car-service billing context, captured per visit (a customer may bring different vehicles,
    // and odometer changes every time) — all optional.
    private String vehicleModel;
    private String registrationNumber;
    private Integer odometerReading;

    private BigDecimal totalAmount;

    private BigDecimal subtotal;
    private BigDecimal discountAmount;
    private BigDecimal taxAmount;
    private BigDecimal grandTotal;
    private BigDecimal paidAmount;
    private BigDecimal balanceAmount;

    private LocalDateTime saleDate = LocalDateTime.now();
}