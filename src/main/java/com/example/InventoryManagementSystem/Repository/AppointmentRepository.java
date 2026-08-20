package com.example.InventoryManagementSystem.Repository;

import com.example.InventoryManagementSystem.model.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    List<Appointment> findByAppointmentDate(LocalDate date);

    List<Appointment> findByCustomerId(Long customerId);
}
