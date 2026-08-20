package com.example.InventoryManagementSystem.service;

import com.example.InventoryManagementSystem.dto.InspectionItemRequestDTO;
import com.example.InventoryManagementSystem.dto.InspectionItemResponseDTO;

import java.util.List;

public interface InspectionItemService {

    // Upserts by (jobCardId, category) — one row per category, no duplicates to manage client-side.
    InspectionItemResponseDTO saveInspectionItem(InspectionItemRequestDTO dto);

    List<InspectionItemResponseDTO> getByJobCardId(Long jobCardId);

    void deleteInspectionItem(Long id);
}
