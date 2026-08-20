package com.example.InventoryManagementSystem.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "sales_return_items")
@Getter
@Setter
public class SalesReturnItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long salesReturnItemId;

    private Long salesReturnId;
    private Long saleId;

    // Set instead of saleId when the return is against an Invoice-based transaction (the
    // current primary billing engine) rather than the legacy Sales module.
    private Long invoiceId;
    private Long invoiceItemId;

    private Long productId;

    private Integer quantity;

    private BigDecimal price;
    private BigDecimal total;
}