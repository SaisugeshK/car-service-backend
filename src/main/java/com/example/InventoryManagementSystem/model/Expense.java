package com.example.InventoryManagementSystem.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

// An outgoing, non-revenue cost (rent, utilities, maintenance, supplies, ...). Kept entirely apart
// from invoices/revenue so an EMPLOYEE can record what they spent without seeing any financial
// totals. Who created it is snapshotted (id, name, role) so the record stays readable even if the
// user is later renamed, re-roled or removed.
@Entity
@Table(name = "expenses")
@Getter
@Setter
@NoArgsConstructor
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long expenseId;

    @Column(nullable = false, length = 150)
    private String title;

    // UTILITIES / RENT / MAINTENANCE / SUPPLIES / MISCELLANEOUS
    @Column(nullable = false, length = 30)
    private String category;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    // CASH / CARD / UPI / BANK_TRANSFER
    @Column(nullable = false, length = 20)
    private String paymentMethod;

    @Column(nullable = false)
    private LocalDate expenseDate;

    // Receipt (PDF/image), stored on disk under a generated name — never the user-supplied one.
    private String receiptStoredFileName;
    private String receiptOriginalFileName;
    private String receiptContentType;
    private Long receiptFileSize;

    @Column(nullable = false)
    private Long createdByUserId;

    private String createdByName;

    // SUPER_ADMIN / EMPLOYEE at the time of entry.
    @Column(length = 30)
    private String createdByRole;

    @Column(updatable = false)
    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        OffsetDateTime now = OffsetDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = OffsetDateTime.now();
    }
}
