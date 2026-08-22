package com.example.InventoryManagementSystem.service;

import com.example.InventoryManagementSystem.dto.ComplaintRequestDTO;
import com.example.InventoryManagementSystem.dto.ComplaintResponseDTO;

import java.util.List;

public interface ComplaintService {

    ComplaintResponseDTO create(ComplaintRequestDTO dto);

    ComplaintResponseDTO update(Long id, ComplaintRequestDTO dto);

    List<ComplaintResponseDTO> getAll();

    ComplaintResponseDTO getById(Long id);

    void delete(Long id);
}
