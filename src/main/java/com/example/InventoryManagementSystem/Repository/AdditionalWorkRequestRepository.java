package com.example.InventoryManagementSystem.Repository;

import com.example.InventoryManagementSystem.model.AdditionalWorkRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AdditionalWorkRequestRepository extends JpaRepository<AdditionalWorkRequest, Long> {

    List<AdditionalWorkRequest> findByJobCardIdOrderByRequestedAtDesc(Long jobCardId);
}
