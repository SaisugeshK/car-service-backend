package com.example.InventoryManagementSystem.service;

import com.example.InventoryManagementSystem.dto.InvoiceResponseDTO;
import com.example.InventoryManagementSystem.dto.JobCardRequestDTO;
import com.example.InventoryManagementSystem.dto.JobCardResponseDTO;

import java.math.BigDecimal;
import java.util.List;

public interface JobCardService {

    JobCardResponseDTO createJobCard(JobCardRequestDTO dto);

    JobCardResponseDTO getJobCardById(Long id);

    List<JobCardResponseDTO> getAllJobCards();

    JobCardResponseDTO updateJobCard(Long id, JobCardRequestDTO dto);

    JobCardResponseDTO updateStatus(Long id, String status);

    void deleteJobCard(Long id);

    // Prefills a new Job Card from an Appointment (customer/vehicle/complaint carried over so
    // nobody re-types it) and marks the appointment ARRIVED/linked.
    JobCardResponseDTO createFromAppointment(Long appointmentId);

    // Converts the job card's latest APPROVED estimate into the one real billing document —
    // reuses InvoiceServiceImpl.createInvoice rather than duplicating that cascade.
    InvoiceResponseDTO generateInvoice(Long jobCardId, String paymentMethod, BigDecimal paidAmount, Long counterId);

    // Marks DELIVERED, updates the vehicle's odometer, and creates the next service reminder.
    JobCardResponseDTO markDelivered(Long jobCardId);
}
