package com.example.InventoryManagementSystem.model;


import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "customers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long customerId;

    @Column(nullable = false, length = 100)
    private String customerName;

    private String phone;

    // WhatsApp is frequently a different number than the primary phone (family/shared devices) —
    // kept separate so estimate/reminder sends (Phase 5/14) target the right channel.
    private String whatsappNumber;

    private String alternateMobile;

    private String email;

    @Column(columnDefinition = "TEXT")
    private String address;

    private String city;

    private String state;

    private String pincode;

    private String gstin;

    @Column(columnDefinition = "TEXT")
    private String notes;

    private String status = "active";

    // Set by the service workflow when an invoice/job card completes for this customer — not
    // user-editable via the Customer form.
    private OffsetDateTime lastServiceDate;

    private OffsetDateTime createdAt = OffsetDateTime.now();
}