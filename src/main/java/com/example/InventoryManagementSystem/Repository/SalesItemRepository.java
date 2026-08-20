package com.example.InventoryManagementSystem.Repository;

import com.example.InventoryManagementSystem.model.SalesItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SalesItemRepository extends JpaRepository<SalesItem, Long> {

    List<SalesItem> findBySaleId(Long saleId);
}