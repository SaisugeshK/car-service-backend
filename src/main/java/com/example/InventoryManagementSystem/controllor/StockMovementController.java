package com.example.InventoryManagementSystem.controllor;

import com.example.InventoryManagementSystem.dto.StockMovementRequest;
import com.example.InventoryManagementSystem.dto.StockMovementResponse;
import com.example.InventoryManagementSystem.service.StockMovementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stock-movements")
@RequiredArgsConstructor
public class StockMovementController {

    private final StockMovementService stockMovementService;

    @PostMapping
    public ResponseEntity<StockMovementResponse>
    createStockMovement(
            @Valid @RequestBody
            StockMovementRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(stockMovementService
                        .createStockMovement(request));
    }

    @GetMapping("/{movementId}")
    public ResponseEntity<StockMovementResponse>
    getStockMovementById(
            @PathVariable Long movementId) {

        return ResponseEntity.ok(
                stockMovementService
                        .getStockMovementById(movementId));
    }

    @GetMapping
    public ResponseEntity<List<StockMovementResponse>>
    getAllStockMovements() {

        return ResponseEntity.ok(
                stockMovementService
                        .getAllStockMovements());
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<List<StockMovementResponse>>
    getByProductId(
            @PathVariable Long productId) {

        return ResponseEntity.ok(
                stockMovementService
                        .getByProductId(productId));
    }

    @DeleteMapping("/{movementId}")
    public ResponseEntity<String>
    deleteStockMovement(
            @PathVariable Long movementId) {

        stockMovementService
                .deleteStockMovement(movementId);

        return ResponseEntity.ok(
                "Stock movement deleted successfully");
    }
}