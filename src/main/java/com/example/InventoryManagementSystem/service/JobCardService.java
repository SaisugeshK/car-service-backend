package com.example.InventoryManagementSystem.service;

import com.example.InventoryManagementSystem.dto.DeliveryChecklistDTO;
import com.example.InventoryManagementSystem.dto.InvoiceResponseDTO;
import com.example.InventoryManagementSystem.dto.JobCardRequestDTO;
import com.example.InventoryManagementSystem.dto.JobCardResponseDTO;
import com.example.InventoryManagementSystem.dto.JobCardStatusHistoryResponseDTO;

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

    // The customer rejected the estimate — bills ONLY the inspection fee, never the rejected
    // work. Refuses to run if any estimate for this job card is APPROVED (use generateInvoice
    // instead) or still awaiting a decision (PENDING/CHANGES_REQUESTED).
    InvoiceResponseDTO generateInspectionFeeInvoice(Long jobCardId, String paymentMethod, BigDecimal paidAmount, Long counterId, BigDecimal feeAmount);

    // Marks DELIVERED, updates the vehicle's odometer, and creates the next service reminder.
    // Requires the delivery checklist (who delivered it, cleaning/belongings/keys all confirmed)
    // — refuses if any of it is missing, same as the existing invoice-generated check.
    JobCardResponseDTO markDelivered(Long jobCardId, DeliveryChecklistDTO checklist);

    // Every status transition this job card actually went through, oldest first — powers the
    // visual timeline.
    List<JobCardStatusHistoryResponseDTO> getStatusHistory(Long jobCardId);
}
