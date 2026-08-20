package com.example.InventoryManagementSystem.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "purchase_returns")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseReturn {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "purchase_return_id")
    private Integer purchaseReturnId;

    @Column(name = "purchase_id")
    private Integer purchaseId;

    @Column(name = "supplier_id")
    private Integer supplierId;

    @Column(name = "return_date")
    private LocalDateTime returnDate;

    @Column(name = "total_amount")
    private BigDecimal totalAmount;

    @Column(name = "notes")
    private String notes;
}