package com.example.InventoryManagementSystem.Repository;

import com.example.InventoryManagementSystem.model.OfferCampaign;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OfferCampaignRepository extends JpaRepository<OfferCampaign, Long> {

    List<OfferCampaign> findByOfferIdOrderByLaunchedAtDesc(Long offerId);
}
