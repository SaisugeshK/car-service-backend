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

    // Marks the estimate CHANGES_REQUESTED and records what the customer wants changed — does
    // not itself create a new estimate. The actual revised estimate is a normal reviseEstimate call.
    EstimateResponseDTO requestChanges(Long id, String notes);

    // Creates a new estimate row (its own items, its own totals) in the same revision chain as
    // originalId, one revisionNumber higher, same estimateNumber. originalId's row is untouched.
    EstimateResponseDTO reviseEstimate(Long originalId, EstimateRequestDTO dto);

    // Every revision of the estimate that estimateId belongs to, oldest first.
    List<EstimateResponseDTO> getRevisions(Long estimateId);

    void deleteEstimate(Long id);
}
