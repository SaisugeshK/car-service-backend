package com.example.InventoryManagementSystem.Repository;

import com.example.InventoryManagementSystem.model.SalesReturnItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SalesReturnItemRepository extends JpaRepository<SalesReturnItem, Long> {

    List<SalesReturnItem> findBySalesReturnId(Long salesReturnId);
}