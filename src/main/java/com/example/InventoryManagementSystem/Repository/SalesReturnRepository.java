package com.example.InventoryManagementSystem.Repository;

import com.example.InventoryManagementSystem.model.SalesReturn;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SalesReturnRepository
        extends JpaRepository<SalesReturn, Long> {
}