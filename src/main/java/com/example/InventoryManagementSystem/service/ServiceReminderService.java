package com.example.InventoryManagementSystem.service;

import com.example.InventoryManagementSystem.dto.ServiceReminderRequestDTO;
import com.example.InventoryManagementSystem.dto.ServiceReminderResponseDTO;

import java.util.List;

public interface ServiceReminderService {

    ServiceReminderResponseDTO createReminder(ServiceReminderRequestDTO dto);

    ServiceReminderResponseDTO getReminderById(Long id);

    List<ServiceReminderResponseDTO> getAllReminders();

    ServiceReminderResponseDTO updateReminder(Long id, ServiceReminderRequestDTO dto);

    void deleteReminder(Long id);
}
