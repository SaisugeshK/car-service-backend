package com.example.InventoryManagementSystem.Repository;

import com.example.InventoryManagementSystem.model.ProductTax;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductTaxRepository extends JpaRepository<ProductTax, Long> {

    // Get all taxes for a product (Billing use)
    List<ProductTax> findByProductId(Long productId);

    // Prevent duplicate tax entry (IMPORTANT FIX)
    Optional<ProductTax> findByProductIdAndTaxName(Long productId, String taxName);

    // Optional: get single tax for product (if only one GST per product)
    Optional<ProductTax> findTopByProductId(Long productId);
}