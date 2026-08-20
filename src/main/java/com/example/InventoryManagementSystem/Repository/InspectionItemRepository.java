package com.example.InventoryManagementSystem.Repository;

import com.example.InventoryManagementSystem.model.InspectionItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InspectionItemRepository extends JpaRepository<InspectionItem, Long> {

    List<InspectionItem> findByJobCardId(Long jobCardId);

    Optional<InspectionItem> findByJobCardIdAndCategory(Long jobCardId, String category);
}
