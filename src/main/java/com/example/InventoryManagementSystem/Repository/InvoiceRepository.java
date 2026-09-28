package com.example.InventoryManagementSystem.Repository;

import com.example.InventoryManagementSystem.model.Invoice;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    // Offer usage = invoices that redeemed it and weren't cancelled (a cancelled invoice frees
    // its use back up).
    long countByOfferIdAndStatusNot(Long offerId, String status);
}
