package com.example.InventoryManagementSystem.Repository;

import com.example.InventoryManagementSystem.model.LeaveRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {

    List<LeaveRequest> findByUserId(Long userId);

    // Used by PayrollCalculationService — every APPROVED leave that overlaps the pay period at
    // all, so a leave spanning a month boundary is still counted correctly on both sides.
    @Query("SELECT l FROM LeaveRequest l WHERE l.userId = :userId AND l.status = 'APPROVED' "
            + "AND l.startDate <= :periodEnd AND l.endDate >= :periodStart")
    List<LeaveRequest> findApprovedOverlapping(@Param("userId") Long userId,
                                                @Param("periodStart") LocalDate periodStart,
                                                @Param("periodEnd") LocalDate periodEnd);
}
