package com.example.InventoryManagementSystem.Repository;

import com.example.InventoryManagementSystem.model.Estimate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EstimateRepository extends JpaRepository<Estimate, Long> {

    List<Estimate> findByJobCardId(Long jobCardId);

    List<Estimate> findByRootEstimateId(Long rootEstimateId);
}
