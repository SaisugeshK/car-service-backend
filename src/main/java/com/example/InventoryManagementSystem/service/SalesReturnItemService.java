package com.example.InventoryManagementSystem.service;

import com.example.InventoryManagementSystem.dto.SalesReturnItemRequestDTO;
import com.example.InventoryManagementSystem.dto.SalesReturnItemResponseDTO;

import java.util.List;
public interface SalesReturnItemService {

    SalesReturnItemResponseDTO createItem(SalesReturnItemRequestDTO dto);

    List<SalesReturnItemResponseDTO> getAll();

    List<SalesReturnItemResponseDTO> getByReturnId(Long salesReturnId);

    SalesReturnItemResponseDTO updateItem(Long id, SalesReturnItemRequestDTO dto);

    void deleteItem(Long id);
}