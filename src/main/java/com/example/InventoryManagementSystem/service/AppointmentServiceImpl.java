package com.example.InventoryManagementSystem.service;

import com.example.InventoryManagementSystem.Repository.AppointmentRepository;
import com.example.InventoryManagementSystem.Repository.CustomerRepository;
import com.example.InventoryManagementSystem.Repository.VehicleRepository;
import com.example.InventoryManagementSystem.dto.AppointmentRequestDTO;
import com.example.InventoryManagementSystem.dto.AppointmentResponseDTO;
import com.example.InventoryManagementSystem.exception.ResourceNotFoundException;
import com.example.InventoryManagementSystem.model.Appointment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AppointmentServiceImpl implements AppointmentService {

    private final AppointmentRepository repository;
    private final CustomerRepository customerRepository;
    private final VehicleRepository vehicleRepository;

    @Override
    public AppointmentResponseDTO createAppointment(AppointmentRequestDTO dto) {
        Appointment appointment = new Appointment();
        appointment.setCustomerId(dto.getCustomerId());
        appointment.setVehicleId(dto.getVehicleId());
        appointment.setPhone(dto.getPhone());
        appointment.setAppointmentDate(dto.getAppointmentDate());
        appointment.setAppointmentTime(dto.getAppointmentTime());
        appointment.setRequestedService(dto.getRequestedService());
        appointment.setNotes(dto.getNotes());
        appointment.setAdvisorUserId(dto.getAdvisorUserId());
        appointment.setStatus(dto.getStatus() != null ? dto.getStatus() : "BOOKED");

        return mapToDto(repository.save(appointment));
    }

    @Override
    public AppointmentResponseDTO getAppointmentById(Long id) {
        return mapToDto(repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id: " + id)));
    }

    @Override
    public List<AppointmentResponseDTO> getAllAppointments() {
        return repository.findAll().stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Override
    public AppointmentResponseDTO updateAppointment(Long id, AppointmentRequestDTO dto) {
        Appointment appointment = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id: " + id));

        if (dto.getCustomerId() != null) appointment.setCustomerId(dto.getCustomerId());
        if (dto.getVehicleId() != null) appointment.setVehicleId(dto.getVehicleId());
        if (dto.getPhone() != null) appointment.setPhone(dto.getPhone());
        if (dto.getAppointmentDate() != null) appointment.setAppointmentDate(dto.getAppointmentDate());
        if (dto.getAppointmentTime() != null) appointment.setAppointmentTime(dto.getAppointmentTime());
        if (dto.getRequestedService() != null) appointment.setRequestedService(dto.getRequestedService());
        if (dto.getNotes() != null) appointment.setNotes(dto.getNotes());
        if (dto.getAdvisorUserId() != null) appointment.setAdvisorUserId(dto.getAdvisorUserId());
        if (dto.getStatus() != null) appointment.setStatus(dto.getStatus());

        return mapToDto(repository.save(appointment));
    }

    @Override
    public void deleteAppointment(Long id) {
        Appointment appointment = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id: " + id));
        repository.delete(appointment);
    }

    private AppointmentResponseDTO mapToDto(Appointment appointment) {
        AppointmentResponseDTO dto = new AppointmentResponseDTO();
        dto.setAppointmentId(appointment.getAppointmentId());
        dto.setCustomerId(appointment.getCustomerId());
        dto.setVehicleId(appointment.getVehicleId());
        dto.setPhone(appointment.getPhone());
        dto.setAppointmentDate(appointment.getAppointmentDate());
        dto.setAppointmentTime(appointment.getAppointmentTime());
        dto.setRequestedService(appointment.getRequestedService());
        dto.setNotes(appointment.getNotes());
        dto.setAdvisorUserId(appointment.getAdvisorUserId());
        dto.setStatus(appointment.getStatus());
        dto.setJobCardId(appointment.getJobCardId());
        dto.setCreatedAt(appointment.getCreatedAt());

        if (appointment.getCustomerId() != null) {
            customerRepository.findById(appointment.getCustomerId())
                    .ifPresent(c -> dto.setCustomerName(c.getCustomerName()));
        }
        if (appointment.getVehicleId() != null) {
            vehicleRepository.findById(appointment.getVehicleId()).ifPresent(v -> {
                dto.setVehicleModel(v.getVehicleModel());
                dto.setRegistrationNumber(v.getRegistrationNumber());
            });
        }

        return dto;
    }
}
