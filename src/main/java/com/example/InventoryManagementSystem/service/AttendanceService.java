package com.example.InventoryManagementSystem.service;

import com.example.InventoryManagementSystem.dto.AttendanceRequestDTO;
import com.example.InventoryManagementSystem.dto.AttendanceResponseDTO;

import java.util.List;

public interface AttendanceService {

    AttendanceResponseDTO create(AttendanceRequestDTO dto);

    AttendanceResponseDTO getById(Long id);

    List<AttendanceResponseDTO> getAll();

    List<AttendanceResponseDTO> getByUserId(Long userId);

    AttendanceResponseDTO update(Long id, AttendanceRequestDTO dto);

    void delete(Long id);
}
