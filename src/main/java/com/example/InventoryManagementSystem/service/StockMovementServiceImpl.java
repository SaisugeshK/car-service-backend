package com.example.InventoryManagementSystem.service;

import com.example.InventoryManagementSystem.dto.StockMovementRequest;
import com.example.InventoryManagementSystem.dto.StockMovementResponse;
import com.example.InventoryManagementSystem.exception.ResourceNotFoundException;
import com.example.InventoryManagementSystem.model.Product;
import com.example.InventoryManagementSystem.model.StockMovement;
import com.example.InventoryManagementSystem.Repository.ProductRepository;
import com.example.InventoryManagementSystem.Repository.StockMovementRepository;
import com.example.InventoryManagementSystem.service.StockMovementService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StockMovementServiceImpl
        implements StockMovementService {

    private final StockMovementRepository stockMovementRepository;
    private final ProductRepository productRepository;

    // This endpoint now backs the controlled "Stock Adjustment" screen only (Purchase/Invoice/
    // Returns already move stock directly and don't route through here) — so unlike before, a
    // movement here actually changes Product.stockQuantity, not just logs an entry.
    @Override
    @Transactional
    public StockMovementResponse createStockMovement(
            StockMovementRequest request) {

        Product product = productRepository.findById(
                        request.getProductId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Product not found"));

        String type = request.getMovementType() != null ? request.getMovementType().toUpperCase() : "";
        int current = product.getStockQuantity() != null ? product.getStockQuantity() : 0;
        int delta = type.contains("OUT") ? -request.getQuantity() : request.getQuantity();
        int updated = current + delta;
        if (updated < 0) {
            throw new IllegalArgumentException(
                    "Insufficient stock for " + product.getProductName()
                            + " (available: " + current + ", requested OUT: " + request.getQuantity() + ")");
        }
        product.setStockQuantity(updated);
        productRepository.save(product);

        StockMovement stockMovement = StockMovement.builder()
                .product(product)
                .movementType(request.getMovementType())
                .quantity(request.getQuantity())
                .referenceId(request.getReferenceId())
                .notes(request.getNotes())
                .build();

        StockMovement saved =
                stockMovementRepository.save(stockMovement);

        return mapToResponse(saved);
    }

    @Override
    public StockMovementResponse getStockMovementById(
            Long movementId) {

        StockMovement stockMovement =
                stockMovementRepository.findById(movementId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Stock movement not found"));

        return mapToResponse(stockMovement);
    }

    @Override
    public List<StockMovementResponse>
    getAllStockMovements() {

        return stockMovementRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public List<StockMovementResponse> getByProductId(
            Long productId) {

        return stockMovementRepository
                .findByProduct_ProductId(productId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional
    public void deleteStockMovement(Long movementId) {

        StockMovement stockMovement =
                stockMovementRepository.findById(movementId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Stock movement not found"));

        // Reverse the adjustment this movement made, same as every other stock-affecting delete
        // in this codebase (e.g. SalesItemServiceImpl, PurchaseReturnItemServiceImpl).
        Product product = stockMovement.getProduct();
        String type = stockMovement.getMovementType() != null ? stockMovement.getMovementType().toUpperCase() : "";
        int current = product.getStockQuantity() != null ? product.getStockQuantity() : 0;
        int reversedDelta = type.contains("OUT") ? stockMovement.getQuantity() : -stockMovement.getQuantity();
        product.setStockQuantity(current + reversedDelta);
        productRepository.save(product);

        stockMovementRepository.delete(stockMovement);
    }

    private StockMovementResponse mapToResponse(
            StockMovement stockMovement) {

        return StockMovementResponse.builder()
                .movementId(stockMovement.getMovementId())
                .productId(stockMovement.getProduct()
                        .getProductId())
                .productName(stockMovement.getProduct()
                        .getProductName())
                .movementType(stockMovement.getMovementType())
                .quantity(stockMovement.getQuantity())
                .referenceId(stockMovement.getReferenceId())
                .notes(stockMovement.getNotes())
                .createdAt(stockMovement.getCreatedAt())
                .build();
    }
}