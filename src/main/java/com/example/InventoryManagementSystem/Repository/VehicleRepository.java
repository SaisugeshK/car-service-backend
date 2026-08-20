package com.example.InventoryManagementSystem.Repository;

import com.example.InventoryManagementSystem.model.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

    List<Vehicle> findByCustomerId(Long customerId);
}
