package com.example.InventoryManagementSystem.Repository;

import com.example.InventoryManagementSystem.model.BillingCounter;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BillingCounterRepository extends JpaRepository<BillingCounter, Long> {

}