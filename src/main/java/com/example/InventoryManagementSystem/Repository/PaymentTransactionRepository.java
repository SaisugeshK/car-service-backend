package com.example.InventoryManagementSystem.Repository;

import com.example.InventoryManagementSystem.model.PaymentTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {
}