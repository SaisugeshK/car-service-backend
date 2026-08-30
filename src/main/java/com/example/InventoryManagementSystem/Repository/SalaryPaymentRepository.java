package com.example.InventoryManagementSystem.Repository;

import com.example.InventoryManagementSystem.model.SalaryPayment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SalaryPaymentRepository extends JpaRepository<SalaryPayment, Long> {

    // The idempotency check — belt-and-suspenders alongside the DB unique constraint on
    // (user_id, pay_period_month, pay_period_year).
    boolean existsByUserIdAndPayPeriodMonthAndPayPeriodYear(Long userId, Integer month, Integer year);

    Optional<SalaryPayment> findByUserIdAndPayPeriodMonthAndPayPeriodYear(Long userId, Integer month, Integer year);

    List<SalaryPayment> findByUserId(Long userId);

    List<SalaryPayment> findByPayPeriodMonthAndPayPeriodYear(Integer month, Integer year);
}
