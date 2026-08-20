package com.example.InventoryManagementSystem.Repository;

import com.example.InventoryManagementSystem.model.PurchaseReturnItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PurchaseReturnItemRepository
        extends JpaRepository<PurchaseReturnItem, Integer> {
}