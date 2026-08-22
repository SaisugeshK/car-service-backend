package com.example.InventoryManagementSystem.service;

import com.example.InventoryManagementSystem.dto.OfferCampaignResponseDTO;
import com.example.InventoryManagementSystem.dto.OfferLaunchRequestDTO;
import com.example.InventoryManagementSystem.dto.OfferRequestDTO;
import com.example.InventoryManagementSystem.dto.OfferResponseDTO;

import java.util.List;

public interface OfferService {

    OfferResponseDTO create(OfferRequestDTO dto);

    OfferResponseDTO update(Long id, OfferRequestDTO dto);

    List<OfferResponseDTO> getAll();

    OfferResponseDTO getById(Long id);

    void delete(Long id);

    // Sends the offer to every eligible customer via the given channel, logging one honest
    // NotificationLog row per attempt — never a fabricated delivery status.
    OfferCampaignResponseDTO launch(Long offerId, OfferLaunchRequestDTO dto);

    List<OfferCampaignResponseDTO> getCampaigns(Long offerId);
}
