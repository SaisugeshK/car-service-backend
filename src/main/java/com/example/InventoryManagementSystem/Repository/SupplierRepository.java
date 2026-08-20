package com.example.InventoryManagementSystem.Repository;

import com.example.InventoryManagementSystem.model.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SupplierRepository extends JpaRepository<Supplier, Long> {

    boolean existsByEmail(String email);
}