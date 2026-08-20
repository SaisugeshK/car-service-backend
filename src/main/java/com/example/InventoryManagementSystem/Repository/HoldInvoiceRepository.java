package com.example.InventoryManagementSystem.Repository;

import com.example.InventoryManagementSystem.model.HoldInvoice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HoldInvoiceRepository
        extends JpaRepository<HoldInvoice, Long> {
}