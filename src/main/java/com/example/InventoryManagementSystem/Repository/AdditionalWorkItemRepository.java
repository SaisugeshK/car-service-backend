package com.example.InventoryManagementSystem.Repository;

import com.example.InventoryManagementSystem.model.AdditionalWorkItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AdditionalWorkItemRepository extends JpaRepository<AdditionalWorkItem, Long> {

    List<AdditionalWorkItem> findByAdditionalWorkRequestId(Long additionalWorkRequestId);

    List<AdditionalWorkItem> findByAdditionalWorkRequestIdIn(List<Long> additionalWorkRequestIds);
}
