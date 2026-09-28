package com.example.InventoryManagementSystem.Repository;

import com.example.InventoryManagementSystem.model.Expense;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    List<Expense> findAllByOrderByExpenseDateDescExpenseIdDesc();

    List<Expense> findByCreatedByUserIdOrderByExpenseDateDescExpenseIdDesc(Long createdByUserId);
}
