package com.example.InventoryManagementSystem.Repository;

import com.example.InventoryManagementSystem.model.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

    List<Vehicle> findByCustomerId(Long customerId);

    // List, not Optional — a handful of pre-existing rows already share a registration number
    // from before this guard existed, and a single-result derived query throws
    // IncorrectResultSizeDataAccessException the moment it meets more than one match.
    List<Vehicle> findByRegistrationNumberIgnoreCase(String registrationNumber);
}
