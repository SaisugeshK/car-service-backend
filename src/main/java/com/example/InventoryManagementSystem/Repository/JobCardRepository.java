package com.example.InventoryManagementSystem.Repository;

import com.example.InventoryManagementSystem.model.JobCard;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JobCardRepository extends JpaRepository<JobCard, Long> {

    List<JobCard> findByVehicleId(Long vehicleId);

    List<JobCard> findByCustomerId(Long customerId);

    List<JobCard> findByStatus(String status);
}
