package com.example.InventoryManagementSystem.service;

import com.example.InventoryManagementSystem.dto.SalesItemRequestDTO;
import com.example.InventoryManagementSystem.dto.SalesItemResponseDTO;

import java.util.List;

public interface SalesItemService {

    SalesItemResponseDTO createSalesItem(SalesItemRequestDTO dto);
    SalesItemResponseDTO updateSalesItem(Long id, SalesItemRequestDTO dto);
    List<SalesItemResponseDTO> getAllSalesItems();

    SalesItemResponseDTO getSalesItemById(Long id);

    List<SalesItemResponseDTO> getItemsBySaleId(Long saleId);

    void deleteSalesItem(Long id);
}