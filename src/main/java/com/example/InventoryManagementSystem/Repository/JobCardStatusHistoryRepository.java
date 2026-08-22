package com.example.InventoryManagementSystem.Repository;

import com.example.InventoryManagementSystem.model.JobCardStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JobCardStatusHistoryRepository extends JpaRepository<JobCardStatusHistory, Long> {

    List<JobCardStatusHistory> findByJobCardIdOrderByChangedAtAsc(Long jobCardId);
}
