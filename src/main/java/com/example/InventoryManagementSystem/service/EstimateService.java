package com.example.InventoryManagementSystem.service;

import com.example.InventoryManagementSystem.dto.EstimateRequestDTO;
import com.example.InventoryManagementSystem.dto.EstimateResponseDTO;

import java.util.List;

public interface EstimateService {

    EstimateResponseDTO createEstimate(EstimateRequestDTO dto);

    EstimateResponseDTO getEstimateById(Long id);

    List<EstimateResponseDTO> getAllEstimates();

    List<EstimateResponseDTO> getByJobCardId(Long jobCardId);

    EstimateResponseDTO approve(Long id, String approvedBy);

    EstimateResponseDTO reject(Long id, String notes);

    void deleteEstimate(Long id);
}
