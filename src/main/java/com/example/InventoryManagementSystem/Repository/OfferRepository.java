package com.example.InventoryManagementSystem.Repository;

import com.example.InventoryManagementSystem.model.Offer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OfferRepository extends JpaRepository<Offer, Long> {
}
