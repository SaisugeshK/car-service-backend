package com.example.InventoryManagementSystem.Repository;

import com.example.InventoryManagementSystem.model.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    List<Attendance> findByUserId(Long userId);

    // Used by PayrollCalculationService to resolve one employee's attendance for a pay period
    // without loading their entire history.
    List<Attendance> findByUserIdAndAttendanceDateBetween(Long userId, LocalDate start, LocalDate end);

    Optional<Attendance> findByUserIdAndAttendanceDate(Long userId, LocalDate attendanceDate);

    boolean existsByUserIdAndAttendanceDate(Long userId, LocalDate attendanceDate);
}
