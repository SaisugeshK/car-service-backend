package com.example.InventoryManagementSystem.service;

import com.example.InventoryManagementSystem.dto.PurchaseReturnItemRequestDTO;
import com.example.InventoryManagementSystem.dto.PurchaseReturnItemResponseDTO;

import java.util.List;

public interface PurchaseReturnItemService {

    PurchaseReturnItemResponseDTO createPurchaseReturnItem(
            PurchaseReturnItemRequestDTO requestDTO
    );

    PurchaseReturnItemResponseDTO getPurchaseReturnItemById(Integer id);

    List<PurchaseReturnItemResponseDTO> getAllPurchaseReturnItems();

    PurchaseReturnItemResponseDTO updatePurchaseReturnItem(
            Integer id,
            PurchaseReturnItemRequestDTO requestDTO
    );

    void deletePurchaseReturnItem(Integer id);
}