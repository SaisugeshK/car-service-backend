package com.example.InventoryManagementSystem.service;

import com.example.InventoryManagementSystem.dto.PurchaseRequestDto;
import com.example.InventoryManagementSystem.dto.PurchaseResponseDto;

import java.util.List;

public interface PurchaseService {

    PurchaseResponseDto createPurchase(PurchaseRequestDto request);

    List<PurchaseResponseDto> getAllPurchases();

    PurchaseResponseDto getPurchaseById(Long purchaseId);

    PurchaseResponseDto updatePurchase(
            Long purchaseId,
            PurchaseRequestDto request);

    void deletePurchase(Long purchaseId);
}