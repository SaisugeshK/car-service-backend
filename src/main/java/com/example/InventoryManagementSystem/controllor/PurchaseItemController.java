package com.example.InventoryManagementSystem.controllor;

import com.example.InventoryManagementSystem.dto.PurchaseItemRequestDto;
import com.example.InventoryManagementSystem.dto.PurchaseItemResponseDto;
import com.example.InventoryManagementSystem.service.PurchaseItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/purchase-items")
@RequiredArgsConstructor
public class PurchaseItemController {

    private final PurchaseItemService purchaseItemService;

    // CREATE
    @PostMapping
    public ResponseEntity<PurchaseItemResponseDto>
    createPurchaseItem(
            @RequestBody PurchaseItemRequestDto request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(
                        purchaseItemService
                                .createPurchaseItem(request));
    }

    // GET ALL
    @GetMapping
    public ResponseEntity<List<PurchaseItemResponseDto>>
    getAllPurchaseItems() {

        return ResponseEntity.ok(
                purchaseItemService.getAllPurchaseItems());
    }

    // GET BY ID
    @GetMapping("/{purchaseItemId}")
    public ResponseEntity<PurchaseItemResponseDto>
    getPurchaseItemById(
            @PathVariable Long purchaseItemId) {

        return ResponseEntity.ok(
                purchaseItemService
                        .getPurchaseItemById(purchaseItemId));
    }

    // UPDATE
    @PutMapping("/{purchaseItemId}")
    public ResponseEntity<PurchaseItemResponseDto>
    updatePurchaseItem(
            @PathVariable Long purchaseItemId,
            @RequestBody PurchaseItemRequestDto request) {

        return ResponseEntity.ok(
                purchaseItemService.updatePurchaseItem(
                        purchaseItemId,
                        request));
    }

    // DELETE
    @DeleteMapping("/{purchaseItemId}")
    public ResponseEntity<String> deletePurchaseItem(
            @PathVariable Long purchaseItemId) {

        purchaseItemService.deletePurchaseItem(
                purchaseItemId);

        return ResponseEntity.ok(
                "Purchase Item deleted successfully");
    }
}