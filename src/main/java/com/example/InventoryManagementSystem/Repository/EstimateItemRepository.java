package com.example.InventoryManagementSystem.Repository;

import com.example.InventoryManagementSystem.model.EstimateItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EstimateItemRepository extends JpaRepository<EstimateItem, Long> {

    List<EstimateItem> findByEstimateId(Long estimateId);
}
