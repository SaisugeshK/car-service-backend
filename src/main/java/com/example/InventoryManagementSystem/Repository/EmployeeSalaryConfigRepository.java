package com.example.InventoryManagementSystem.Repository;

import com.example.InventoryManagementSystem.model.EmployeeSalaryConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EmployeeSalaryConfigRepository extends JpaRepository<EmployeeSalaryConfig, Long> {

    Optional<EmployeeSalaryConfig> findByUserId(Long userId);

    boolean existsByUserIdAndActiveTrue(Long userId);

    // Used by PayrollCalculationService to iterate every eligible employee for a payroll run.
    List<EmployeeSalaryConfig> findByActiveTrue();
}
