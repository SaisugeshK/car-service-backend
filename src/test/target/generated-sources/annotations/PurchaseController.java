package com.example.InventoryManagementSystem.controllor;

import com.example.InventoryManagementSystem.dto.PurchaseRequestDto;
import com.example.InventoryManagementSystem.dto.PurchaseResponseDto;
import com.example.InventoryManagementSystem.service.PurchaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/purchases")
@RequiredArgsConstructor
public class PurchaseController {

    private final PurchaseService purchaseService;

    @PostMapping
    public PurchaseResponseDto createPurchase(
            @RequestBody PurchaseRequestDto dto) {

        return purchaseService.createPurchase(dto);
    }

    @GetMapping
    public List<PurchaseResponseDto> getAllPurchases() {

        return purchaseService.getAllPurchases();
    }

    @GetMapping("/{id}")
    public PurchaseResponseDto getPurchaseById(
            @PathVariable Long id) {

        return purchaseService.getPurchaseById(id);
    }

    @PutMapping("/{id}")
    public PurchaseResponseDto updatePurchase(
            @PathVariable Long id,
            @RequestBody PurchaseRequestDto dto) {

        return purchaseService.updatePurchase(id, dto);
    }

    @DeleteMapping("/{id}")
    public String deletePurchase(
            @PathVariable Long id) {

        purchaseService.deletePurchase(id);

        return "Purchase deleted successfully";
    }
}