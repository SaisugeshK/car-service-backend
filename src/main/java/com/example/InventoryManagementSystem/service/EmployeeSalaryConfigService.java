package com.example.InventoryManagementSystem.service;

import com.example.InventoryManagementSystem.dto.EmployeeSalaryConfigRequestDTO;
import com.example.InventoryManagementSystem.dto.EmployeeSalaryConfigResponseDTO;

import java.util.List;

public interface EmployeeSalaryConfigService {

    EmployeeSalaryConfigResponseDTO create(EmployeeSalaryConfigRequestDTO dto);

    EmployeeSalaryConfigResponseDTO getById(Long id);

    EmployeeSalaryConfigResponseDTO getByUserId(Long userId);

    List<EmployeeSalaryConfigResponseDTO> getAll();

    EmployeeSalaryConfigResponseDTO update(Long id, EmployeeSalaryConfigRequestDTO dto);

    // Deactivates rather than deletes (spec §22) — history/audit stays intact, and any already
    // generated SalaryPayment rows keep their own frozen snapshot regardless.
    EmployeeSalaryConfigResponseDTO deactivate(Long id);
}
