package com.example.InventoryManagementSystem.service;

import com.example.InventoryManagementSystem.dto.OfferCampaignResponseDTO;
import com.example.InventoryManagementSystem.dto.OfferCheckRequestDTO;
import com.example.InventoryManagementSystem.dto.OfferEligibilityDTO;
import com.example.InventoryManagementSystem.dto.OfferLaunchRequestDTO;
import com.example.InventoryManagementSystem.dto.OfferRequestDTO;
import com.example.InventoryManagementSystem.dto.OfferResponseDTO;
import com.example.InventoryManagementSystem.model.Offer;

import java.math.BigDecimal;
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

    // Every enabled, not-expired offer for this vehicle type, each checked against the bill —
    // what the billing screen shows so staff never have to type a code for an offer that exists.
    List<OfferEligibilityDTO> getApplicable(OfferCheckRequestDTO bill);

    // Checks one coupon code against the bill. Never throws for an unusable coupon — returns
    // eligible=false with the reason instead.
    OfferEligibilityDTO checkCoupon(OfferCheckRequestDTO bill);

    // Invoice-time redemption: locks the offer row, re-runs every check and returns the
    // discount. Throws IllegalArgumentException with the reason if the coupon can't be used.
    OfferRedemption redeem(String couponCode, BigDecimal billAmount, String vehicleType);

    record OfferRedemption(Offer offer, BigDecimal discountAmount) {}
}
