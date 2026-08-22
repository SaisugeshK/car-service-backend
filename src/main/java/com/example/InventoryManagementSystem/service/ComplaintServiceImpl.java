package com.example.InventoryManagementSystem.service;

import com.example.InventoryManagementSystem.Repository.ComplaintRepository;
import com.example.InventoryManagementSystem.Repository.CustomerRepository;
import com.example.InventoryManagementSystem.Repository.JobCardRepository;
import com.example.InventoryManagementSystem.Repository.UserRepository;
import com.example.InventoryManagementSystem.Repository.VehicleRepository;
import com.example.InventoryManagementSystem.dto.ComplaintRequestDTO;
import com.example.InventoryManagementSystem.dto.ComplaintResponseDTO;
import com.example.InventoryManagementSystem.exception.ResourceNotFoundException;
import com.example.InventoryManagementSystem.model.Complaint;
import com.example.InventoryManagementSystem.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ComplaintServiceImpl implements ComplaintService {

    private final ComplaintRepository repository;
    private final CustomerRepository customerRepository;
    private final VehicleRepository vehicleRepository;
    private final JobCardRepository jobCardRepository;
    private final UserRepository userRepository;

    @Override
    public ComplaintResponseDTO create(ComplaintRequestDTO dto) {
        customerRepository.findById(dto.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + dto.getCustomerId()));

        Complaint complaint = new Complaint();
        complaint.setCustomerId(dto.getCustomerId());
        complaint.setVehicleId(dto.getVehicleId());
        complaint.setJobCardId(dto.getJobCardId());
        complaint.setType(dto.getType());
        complaint.setDescription(dto.getDescription());
        complaint.setPriority(dto.getPriority() != null ? dto.getPriority() : "MEDIUM");
        complaint.setAssignedToUserId(dto.getAssignedToUserId());
        complaint.setStatus(dto.getStatus() != null ? dto.getStatus() : "OPEN");
        complaint.setResolution(dto.getResolution());
        complaint.setResolutionDate(resolveResolutionDate(dto.getStatus(), dto.getResolutionDate()));

        return mapToDto(repository.save(complaint));
    }

    @Override
    public ComplaintResponseDTO update(Long id, ComplaintRequestDTO dto) {
        Complaint complaint = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found with id: " + id));

        if (dto.getVehicleId() != null) complaint.setVehicleId(dto.getVehicleId());
        if (dto.getJobCardId() != null) complaint.setJobCardId(dto.getJobCardId());
        if (dto.getType() != null) complaint.setType(dto.getType());
        if (dto.getDescription() != null) complaint.setDescription(dto.getDescription());
        if (dto.getPriority() != null) complaint.setPriority(dto.getPriority());
        if (dto.getAssignedToUserId() != null) complaint.setAssignedToUserId(dto.getAssignedToUserId());
        if (dto.getResolution() != null) complaint.setResolution(dto.getResolution());

        if (dto.getStatus() != null) {
            // Auto-stamp the resolution date the moment a complaint is actually marked resolved,
            // unless one was explicitly supplied — staff shouldn't have to remember to set it.
            if (("RESOLVED".equals(dto.getStatus()) || "CLOSED".equals(dto.getStatus()))
                    && complaint.getResolutionDate() == null && dto.getResolutionDate() == null) {
                complaint.setResolutionDate(LocalDate.now());
            } else if (dto.getResolutionDate() != null) {
                complaint.setResolutionDate(dto.getResolutionDate());
            }
            complaint.setStatus(dto.getStatus());
        }

        return mapToDto(repository.save(complaint));
    }

    private LocalDate resolveResolutionDate(String status, LocalDate explicit) {
        if (explicit != null) return explicit;
        if ("RESOLVED".equals(status) || "CLOSED".equals(status)) return LocalDate.now();
        return null;
    }

    @Override
    public List<ComplaintResponseDTO> getAll() {
        return repository.findAll().stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Override
    public ComplaintResponseDTO getById(Long id) {
        return mapToDto(repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found with id: " + id)));
    }

    @Override
    public void delete(Long id) {
        Complaint complaint = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found with id: " + id));
        repository.delete(complaint);
    }

    private ComplaintResponseDTO mapToDto(Complaint complaint) {
        ComplaintResponseDTO dto = new ComplaintResponseDTO();
        dto.setComplaintId(complaint.getComplaintId());
        dto.setCustomerId(complaint.getCustomerId());
        dto.setVehicleId(complaint.getVehicleId());
        dto.setJobCardId(complaint.getJobCardId());
        dto.setType(complaint.getType());
        dto.setDescription(complaint.getDescription());
        dto.setPriority(complaint.getPriority());
        dto.setAssignedToUserId(complaint.getAssignedToUserId());
        dto.setStatus(complaint.getStatus());
        dto.setResolution(complaint.getResolution());
        dto.setResolutionDate(complaint.getResolutionDate());
        dto.setCreatedAt(complaint.getCreatedAt());

        customerRepository.findById(complaint.getCustomerId()).ifPresent(c -> dto.setCustomerName(c.getCustomerName()));
        if (complaint.getVehicleId() != null) {
            vehicleRepository.findById(complaint.getVehicleId()).ifPresent(v -> {
                dto.setVehicleModel(v.getVehicleModel());
                dto.setRegistrationNumber(v.getRegistrationNumber());
            });
        }
        if (complaint.getJobCardId() != null) {
            jobCardRepository.findById(complaint.getJobCardId()).ifPresent(jc -> dto.setJobCardNumber(jc.getJobCardNumber()));
        }
        if (complaint.getAssignedToUserId() != null) {
            userRepository.findById(complaint.getAssignedToUserId()).ifPresent(u -> dto.setAssignedToName(displayName(u)));
        }

        return dto;
    }

    private String displayName(User user) {
        if (user.getFullName() != null && !user.getFullName().isBlank()) return user.getFullName();
        return user.getUsername();
    }
}
