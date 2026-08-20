package com.example.InventoryManagementSystem.Repository;

import com.example.InventoryManagementSystem.model.Purchase;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PurchaseRepository extends JpaRepository<Purchase, Long> {
}