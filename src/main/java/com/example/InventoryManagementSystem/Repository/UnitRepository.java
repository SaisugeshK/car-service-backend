package com.example.InventoryManagementSystem.Repository;

import com.example.InventoryManagementSystem.model.Unit;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UnitRepository
        extends JpaRepository<Unit, Long> {
}