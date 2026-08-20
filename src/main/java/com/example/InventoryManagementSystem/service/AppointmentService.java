package com.example.InventoryManagementSystem.service;

import com.example.InventoryManagementSystem.dto.AppointmentRequestDTO;
import com.example.InventoryManagementSystem.dto.AppointmentResponseDTO;

import java.util.List;

public interface AppointmentService {

    AppointmentResponseDTO createAppointment(AppointmentRequestDTO dto);

    AppointmentResponseDTO getAppointmentById(Long id);

    List<AppointmentResponseDTO> getAllAppointments();

    AppointmentResponseDTO updateAppointment(Long id, AppointmentRequestDTO dto);

    void deleteAppointment(Long id);
}
