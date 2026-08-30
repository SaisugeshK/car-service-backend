package com.example.InventoryManagementSystem.Repository;

import com.example.InventoryManagementSystem.model.OvertimeEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface OvertimeEntryRepository extends JpaRepository<OvertimeEntry, Long> {

    List<OvertimeEntry> findByUserId(Long userId);

    // Used by PayrollCalculationService to sum one employee's APPROVED overtime for a pay period.
    List<OvertimeEntry> findByUserIdAndStatusAndWorkDateBetween(Long userId, String status, LocalDate start, LocalDate end);
}
