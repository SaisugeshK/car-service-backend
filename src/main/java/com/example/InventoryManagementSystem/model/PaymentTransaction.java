package com.example.InventoryManagementSystem.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "payment_transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long transactionId;

    private Long invoiceId;

    private String paymentMethod;

    private String transactionReference;

    private BigDecimal amount;

    private OffsetDateTime paymentDate;

    // Staff member who took the payment — a plain reference to Users, same pattern as
    // JobCard.advisorUserId/technicianUserId (picked from a dropdown, not derived from session;
    // this backend's auth doesn't currently surface a numeric user id on login).
    private Long receivedByUserId;

    @Column(columnDefinition = "TEXT")
    private String notes;

    // The field initializer above is silently skipped by Lombok's generated @Builder (a known
    // gotcha — @Builder never runs field initializers unless the field is @Builder.Default), and
    // PaymentTransactionServiceImpl.create() always constructs via .builder(), so paymentDate
    // was coming back null on every new payment. @PrePersist is a real JPA hook that always
    // fires regardless of how the entity was constructed.
    @PrePersist
    public void prePersist() {
        if (paymentDate == null) {
            paymentDate = OffsetDateTime.now();
        }
    }
}