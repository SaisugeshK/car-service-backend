package com.example.InventoryManagementSystem.service;

import com.example.InventoryManagementSystem.dto.StockMovementRequest;
import com.example.InventoryManagementSystem.dto.StockMovementResponse;

import java.util.List;

public interface StockMovementService {

    StockMovementResponse createStockMovement(
            StockMovementRequest request);

    StockMovementResponse getStockMovementById(
            Long movementId);

    List<StockMovementResponse> getAllStockMovements();

    List<StockMovementResponse> getByProductId(
            Long productId);

    void deleteStockMovement(Long movementId);
}