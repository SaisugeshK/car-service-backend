package com.example.InventoryManagementSystem.service;

import com.example.InventoryManagementSystem.dto.PurchaseReturnRequestDTO;
import com.example.InventoryManagementSystem.dto.PurchaseReturnResponseDTO;

import java.util.List;

public interface PurchaseReturnService {

    PurchaseReturnResponseDTO createPurchaseReturn(
            PurchaseReturnRequestDTO requestDTO
    );

    PurchaseReturnResponseDTO getPurchaseReturnById(Integer id);

    List<PurchaseReturnResponseDTO> getAllPurchaseReturns();

    PurchaseReturnResponseDTO updatePurchaseReturn(
            Integer id,
            PurchaseReturnRequestDTO requestDTO
    );

    void deletePurchaseReturn(Integer id);
}