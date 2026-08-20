package com.example.InventoryManagementSystem.service;

import com.example.InventoryManagementSystem.dto.VehicleRequestDTO;
import com.example.InventoryManagementSystem.dto.VehicleResponseDTO;

import java.util.List;

public interface VehicleService {

    VehicleResponseDTO createVehicle(VehicleRequestDTO dto);

    VehicleResponseDTO getVehicleById(Long id);

    List<VehicleResponseDTO> getAllVehicles();

    List<VehicleResponseDTO> getVehiclesByCustomerId(Long customerId);

    VehicleResponseDTO updateVehicle(Long id, VehicleRequestDTO dto);

    void deleteVehicle(Long id);
}
