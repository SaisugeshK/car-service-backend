package com.example.InventoryManagementSystem.Repository;

import com.example.InventoryManagementSystem.model.InvoiceItem;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InvoiceItemRepository
        extends JpaRepository<InvoiceItem, Long> {

    List<InvoiceItem> findByInvoiceId(Integer invoiceId);
}
