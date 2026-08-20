package com.example.InventoryManagementSystem.service;

import com.example.InventoryManagementSystem.dto.QualityCheckRequestDTO;
import com.example.InventoryManagementSystem.dto.QualityCheckResponseDTO;

import java.util.List;

public interface QualityCheckService {

    // FAIL routes the job card back to IN_PROGRESS; PASS moves it to READY_FOR_DELIVERY.
    // Enforced here, not just in the frontend.
    QualityCheckResponseDTO recordCheck(QualityCheckRequestDTO dto);

    List<QualityCheckResponseDTO> getByJobCardId(Long jobCardId);
}
