package com.example.InventoryManagementSystem.Repository;

import com.example.InventoryManagementSystem.model.ServiceReminder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ServiceReminderRepository extends JpaRepository<ServiceReminder, Long> {

    List<ServiceReminder> findByVehicleId(Long vehicleId);
}
