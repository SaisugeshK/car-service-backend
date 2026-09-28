package com.example.InventoryManagementSystem.Repository;

import com.example.InventoryManagementSystem.model.Visit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VisitRepository extends JpaRepository<Visit, Long> {

    List<Visit> findAllByOrderByVisitDateTimeDesc();

    List<Visit> findByCustomerIdOrderByVisitDateTimeDesc(Long customerId);

    List<Visit> findByHandledByUserIdOrderByVisitDateTimeDesc(Long handledByUserId);

    boolean existsByJobCardId(Long jobCardId);

    Optional<Visit> findByJobCardId(Long jobCardId);
}
