package com.example.InventoryManagementSystem.service;

import com.example.InventoryManagementSystem.dto.LeaveRequestRequestDTO;
import com.example.InventoryManagementSystem.dto.LeaveRequestResponseDTO;

import java.util.List;

public interface LeaveRequestService {

    LeaveRequestResponseDTO create(LeaveRequestRequestDTO dto);

    LeaveRequestResponseDTO getById(Long id);

    List<LeaveRequestResponseDTO> getAll();

    List<LeaveRequestResponseDTO> getByUserId(Long userId);

    LeaveRequestResponseDTO update(Long id, LeaveRequestRequestDTO dto);

    LeaveRequestResponseDTO approve(Long id);

    LeaveRequestResponseDTO reject(Long id);

    void delete(Long id);
}
