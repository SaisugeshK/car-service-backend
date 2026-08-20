package com.example.InventoryManagementSystem.service;

import com.example.InventoryManagementSystem.dto.CustomerFollowUpRequestDTO;
import com.example.InventoryManagementSystem.dto.CustomerFollowUpResponseDTO;

import java.util.List;

public interface CustomerFollowUpService {

    CustomerFollowUpResponseDTO createFollowUp(CustomerFollowUpRequestDTO dto);

    CustomerFollowUpResponseDTO getFollowUpById(Long id);

    List<CustomerFollowUpResponseDTO> getAllFollowUps();

    CustomerFollowUpResponseDTO updateFollowUp(Long id, CustomerFollowUpRequestDTO dto);

    void deleteFollowUp(Long id);
}
