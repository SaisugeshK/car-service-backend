package com.example.InventoryManagementSystem.Repository;

import com.example.InventoryManagementSystem.model.StockMovement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StockMovementRepository
        extends JpaRepository<StockMovement, Long> {

    List<StockMovement> findByProduct_ProductId(Long productId);
}