package com.example.InventoryManagementSystem.service;

import com.example.InventoryManagementSystem.dto.ServiceReminderRequestDTO;
import com.example.InventoryManagementSystem.dto.ServiceReminderResponseDTO;

import java.time.LocalDate;
import java.util.List;

public interface ServiceReminderService {

    ServiceReminderResponseDTO createReminder(ServiceReminderRequestDTO dto);

    // Used by other services (VehicleServiceImpl for insurance/PUC, JobCardServiceImpl for
    // next-service/oil-change) to keep exactly one live reminder per vehicle+type instead of
    // piling up a new row every time the source data changes. No-ops if dueDate is null.
    void upsertAutoReminder(Long vehicleId, String reminderType, LocalDate dueDate, Integer dueOdometer, String notes);

    ServiceReminderResponseDTO getReminderById(Long id);

    List<ServiceReminderResponseDTO> getAllReminders();

    ServiceReminderResponseDTO updateReminder(Long id, ServiceReminderRequestDTO dto);

    void deleteReminder(Long id);
}
