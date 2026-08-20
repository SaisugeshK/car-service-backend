package com.example.InventoryManagementSystem.service;

import com.example.InventoryManagementSystem.Repository.CustomerFollowUpRepository;
import com.example.InventoryManagementSystem.Repository.CustomerRepository;
import com.example.InventoryManagementSystem.Repository.VehicleRepository;
import com.example.InventoryManagementSystem.dto.CustomerFollowUpRequestDTO;
import com.example.InventoryManagementSystem.dto.CustomerFollowUpResponseDTO;
import com.example.InventoryManagementSystem.exception.ResourceNotFoundException;
import com.example.InventoryManagementSystem.model.CustomerFollowUp;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomerFollowUpServiceImpl implements CustomerFollowUpService {

    private final CustomerFollowUpRepository repository;
    private final CustomerRepository customerRepository;
    private final VehicleRepository vehicleRepository;

    @Override
    public CustomerFollowUpResponseDTO createFollowUp(CustomerFollowUpRequestDTO dto) {
        CustomerFollowUp followUp = new CustomerFollowUp();
        followUp.setCustomerId(dto.getCustomerId());
        followUp.setVehicleId(dto.getVehicleId());
        followUp.setReminderDate(dto.getReminderDate());
        followUp.setCustomerResponse(dto.getCustomerResponse());
        followUp.setStatus(dto.getStatus() != null ? dto.getStatus() : "PENDING");
        followUp.setNotes(dto.getNotes());
        return mapToDto(repository.save(followUp));
    }

    @Override
    public CustomerFollowUpResponseDTO getFollowUpById(Long id) {
        return mapToDto(repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Follow-up not found with id: " + id)));
    }

    @Override
    public List<CustomerFollowUpResponseDTO> getAllFollowUps() {
        return repository.findAll().stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Override
    public CustomerFollowUpResponseDTO updateFollowUp(Long id, CustomerFollowUpRequestDTO dto) {
        CustomerFollowUp followUp = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Follow-up not found with id: " + id));
        if (dto.getCustomerId() != null) followUp.setCustomerId(dto.getCustomerId());
        if (dto.getVehicleId() != null) followUp.setVehicleId(dto.getVehicleId());
        if (dto.getReminderDate() != null) followUp.setReminderDate(dto.getReminderDate());
        if (dto.getCustomerResponse() != null) followUp.setCustomerResponse(dto.getCustomerResponse());
        if (dto.getStatus() != null) followUp.setStatus(dto.getStatus());
        if (dto.getNotes() != null) followUp.setNotes(dto.getNotes());
        return mapToDto(repository.save(followUp));
    }

    @Override
    public void deleteFollowUp(Long id) {
        CustomerFollowUp followUp = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Follow-up not found with id: " + id));
        repository.delete(followUp);
    }

    private CustomerFollowUpResponseDTO mapToDto(CustomerFollowUp followUp) {
        CustomerFollowUpResponseDTO dto = new CustomerFollowUpResponseDTO();
        dto.setFollowUpId(followUp.getFollowUpId());
        dto.setCustomerId(followUp.getCustomerId());
        dto.setVehicleId(followUp.getVehicleId());
        dto.setReminderDate(followUp.getReminderDate());
        dto.setCustomerResponse(followUp.getCustomerResponse());
        dto.setStatus(followUp.getStatus());
        dto.setNotes(followUp.getNotes());
        dto.setCreatedAt(followUp.getCreatedAt());

        if (followUp.getCustomerId() != null) {
            customerRepository.findById(followUp.getCustomerId()).ifPresent(c -> {
                dto.setCustomerName(c.getCustomerName());
                dto.setCustomerPhone(c.getPhone());
            });
        }
        if (followUp.getVehicleId() != null) {
            vehicleRepository.findById(followUp.getVehicleId()).ifPresent(v -> {
                dto.setVehicleModel(v.getVehicleModel());
                dto.setRegistrationNumber(v.getRegistrationNumber());
            });
        }

        return dto;
    }
}
