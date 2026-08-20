package com.example.InventoryManagementSystem.Repository;

import com.example.InventoryManagementSystem.model.QualityCheck;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QualityCheckRepository extends JpaRepository<QualityCheck, Long> {

    List<QualityCheck> findByJobCardId(Long jobCardId);
}
