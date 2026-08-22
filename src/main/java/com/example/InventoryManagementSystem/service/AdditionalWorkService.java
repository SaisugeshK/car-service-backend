package com.example.InventoryManagementSystem.service;

import com.example.InventoryManagementSystem.dto.AdditionalWorkRequestDTO;
import com.example.InventoryManagementSystem.dto.AdditionalWorkResponseDTO;

import java.util.List;

public interface AdditionalWorkService {

    AdditionalWorkResponseDTO create(AdditionalWorkRequestDTO dto);

    List<AdditionalWorkResponseDTO> getByJobCard(Long jobCardId);

    List<AdditionalWorkResponseDTO> getAll();

    AdditionalWorkResponseDTO approve(Long id, String decidedBy);

    AdditionalWorkResponseDTO reject(Long id, String decidedBy);
}
