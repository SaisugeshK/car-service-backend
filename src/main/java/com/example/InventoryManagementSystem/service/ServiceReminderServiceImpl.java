package com.example.InventoryManagementSystem.service;

import com.example.InventoryManagementSystem.Repository.CustomerRepository;
import com.example.InventoryManagementSystem.Repository.ServiceReminderRepository;
import com.example.InventoryManagementSystem.Repository.VehicleRepository;
import com.example.InventoryManagementSystem.dto.ServiceReminderRequestDTO;
import com.example.InventoryManagementSystem.dto.ServiceReminderResponseDTO;
import com.example.InventoryManagementSystem.exception.ResourceNotFoundException;
import com.example.InventoryManagementSystem.model.ServiceReminder;
import com.example.InventoryManagementSystem.model.Vehicle;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ServiceReminderServiceImpl implements ServiceReminderService {

    private final ServiceReminderRepository repository;
    private final VehicleRepository vehicleRepository;
    private final CustomerRepository customerRepository;

    @Override
    public ServiceReminderResponseDTO createReminder(ServiceReminderRequestDTO dto) {
        ServiceReminder reminder = new ServiceReminder();
        reminder.setVehicleId(dto.getVehicleId());
        reminder.setDueDate(dto.getDueDate());
        reminder.setDueOdometer(dto.getDueOdometer());
        reminder.setSourceInvoiceId(dto.getSourceInvoiceId());
        reminder.setNotes(dto.getNotes());
        reminder.setStatus(dto.getStatus() != null ? dto.getStatus() : "UPCOMING");
        return mapToDto(repository.save(reminder));
    }

    @Override
    public ServiceReminderResponseDTO getReminderById(Long id) {
        return mapToDto(repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Service reminder not found with id: " + id)));
    }

    @Override
    public List<ServiceReminderResponseDTO> getAllReminders() {
        return repository.findAll().stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Override
    public ServiceReminderResponseDTO updateReminder(Long id, ServiceReminderRequestDTO dto) {
        ServiceReminder reminder = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Service reminder not found with id: " + id));
        if (dto.getDueDate() != null) reminder.setDueDate(dto.getDueDate());
        if (dto.getDueOdometer() != null) reminder.setDueOdometer(dto.getDueOdometer());
        if (dto.getNotes() != null) reminder.setNotes(dto.getNotes());
        if (dto.getStatus() != null) reminder.setStatus(dto.getStatus());
        return mapToDto(repository.save(reminder));
    }

    @Override
    public void deleteReminder(Long id) {
        ServiceReminder reminder = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Service reminder not found with id: " + id));
        repository.delete(reminder);
    }

    private ServiceReminderResponseDTO mapToDto(ServiceReminder reminder) {
        ServiceReminderResponseDTO dto = new ServiceReminderResponseDTO();
        dto.setReminderId(reminder.getReminderId());
        dto.setVehicleId(reminder.getVehicleId());
        dto.setDueDate(reminder.getDueDate());
        dto.setDueOdometer(reminder.getDueOdometer());
        dto.setSourceInvoiceId(reminder.getSourceInvoiceId());
        dto.setNotes(reminder.getNotes());
        dto.setCreatedAt(reminder.getCreatedAt());

        // Derive UPCOMING/DUE/OVERDUE fresh from today's date rather than trusting a stored
        // value that would otherwise go stale; DONE is the one status left untouched.
        if ("DONE".equals(reminder.getStatus())) {
            dto.setStatus("DONE");
        } else if (reminder.getDueDate() != null) {
            LocalDate today = LocalDate.now();
            if (today.isAfter(reminder.getDueDate())) {
                dto.setStatus("OVERDUE");
            } else if (!today.isBefore(reminder.getDueDate().minusDays(14))) {
                dto.setStatus("DUE");
            } else {
                dto.setStatus("UPCOMING");
            }
        } else {
            dto.setStatus(reminder.getStatus());
        }

        Vehicle vehicle = vehicleRepository.findById(reminder.getVehicleId()).orElse(null);
        if (vehicle != null) {
            dto.setVehicleModel(vehicle.getVehicleModel());
            dto.setRegistrationNumber(vehicle.getRegistrationNumber());
            dto.setCustomerId(vehicle.getCustomerId());
            customerRepository.findById(vehicle.getCustomerId()).ifPresent(c -> {
                dto.setCustomerName(c.getCustomerName());
                dto.setCustomerPhone(c.getPhone());
            });
        }

        return dto;
    }
}
