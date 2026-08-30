package com.example.InventoryManagementSystem.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "invoices")
@Getter
@Setter
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "invoice_id")
    private Long invoiceId;

    // Pre-deployment fix — concurrency testing found two truly-simultaneous payments against the
    // same invoice could both read the same paidAmount before either committed, so the second
    // write silently clobbered the first's contribution to paidAmount/balanceAmount (a classic
    // lost update). @Version makes the second save fail cleanly instead — see
    // PaymentTransactionServiceImpl.applyToInvoice.
    @Version
    private Long version;

    @Column(name = "invoice_number", nullable = false, unique = true, length = 100)
    private String invoiceNumber;

    @Column(name = "customer_id")
    private Integer customerId;

    @Column(name = "vehicle_id")
    private Long vehicleId;

    // Snapshotted at time of visit — Vehicle.odometer holds the latest reading for next-visit
    // reference, but a printed/reprinted invoice must always show what it was AT THAT VISIT.
    @Column(name = "odometer_reading")
    private Integer odometerReading;

    @Column(name = "counter_id")
    private Integer counterId;

    @Column(name = "invoice_date")
    private OffsetDateTime invoiceDate;

    @Column(name = "subtotal", precision = 12, scale = 2)
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Column(name = "discount_amount", precision = 12, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "tax_amount", precision = 12, scale = 2)
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Column(name = "cgst_amount", precision = 12, scale = 2)
    private BigDecimal cgstAmount = BigDecimal.ZERO;

    @Column(name = "sgst_amount", precision = 12, scale = 2)
    private BigDecimal sgstAmount = BigDecimal.ZERO;

    @Column(name = "grand_total", precision = 12, scale = 2)
    private BigDecimal grandTotal = BigDecimal.ZERO;

    @Column(name = "paid_amount", precision = 12, scale = 2)
    private BigDecimal paidAmount = BigDecimal.ZERO;

    @Column(name = "balance_amount", precision = 12, scale = 2)
    private BigDecimal balanceAmount = BigDecimal.ZERO;

    @Column(name = "payment_method", length = 30)
    private String paymentMethod;

    @Column(name = "payment_status", length = 20)
    private String paymentStatus = "PAID";

    // Invoice lifecycle — distinct from paymentStatus (which tracks money owed). COMPLETED or
    // CANCELLED; a held-but-not-yet-billed cart lives in hold_invoices, never here.
    @Column(name = "status", length = 20)
    private String status = "COMPLETED";

    @Column(name = "created_by")
    private Integer createdBy;

    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        OffsetDateTime now = OffsetDateTime.now();
        createdAt = now;
        updatedAt = now;
        if (invoiceDate == null) {
            invoiceDate = now;
        }
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = OffsetDateTime.now();
    }
}
