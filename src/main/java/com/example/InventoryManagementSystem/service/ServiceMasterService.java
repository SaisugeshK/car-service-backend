package com.example.InventoryManagementSystem.service;

import com.example.InventoryManagementSystem.dto.ServiceMasterRequestDTO;
import com.example.InventoryManagementSystem.dto.ServiceMasterResponseDTO;

import java.util.List;

public interface ServiceMasterService {

    ServiceMasterResponseDTO createService(ServiceMasterRequestDTO dto);

    ServiceMasterResponseDTO getServiceById(Long id);

    List<ServiceMasterResponseDTO> getAllServices();

    ServiceMasterResponseDTO updateService(Long id, ServiceMasterRequestDTO dto);

    void deleteService(Long id);
}
