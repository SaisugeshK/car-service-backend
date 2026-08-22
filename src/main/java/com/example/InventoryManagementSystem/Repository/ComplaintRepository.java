package com.example.InventoryManagementSystem.Repository;

import com.example.InventoryManagementSystem.model.Complaint;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ComplaintRepository extends JpaRepository<Complaint, Long> {
}
