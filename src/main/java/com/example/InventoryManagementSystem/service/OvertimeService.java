package com.example.InventoryManagementSystem.service;

import com.example.InventoryManagementSystem.dto.OvertimeRequestDTO;
import com.example.InventoryManagementSystem.dto.OvertimeResponseDTO;

import java.util.List;

public interface OvertimeService {

    OvertimeResponseDTO create(OvertimeRequestDTO dto);

    OvertimeResponseDTO getById(Long id);

    List<OvertimeResponseDTO> getAll();

    List<OvertimeResponseDTO> getByUserId(Long userId);

    OvertimeResponseDTO update(Long id, OvertimeRequestDTO dto);

    OvertimeResponseDTO approve(Long id);

    OvertimeResponseDTO reject(Long id);

    void delete(Long id);
}
