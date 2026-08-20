package com.example.InventoryManagementSystem.service;

import com.example.InventoryManagementSystem.Repository.ServiceMasterRepository;
import com.example.InventoryManagementSystem.dto.ServiceMasterRequestDTO;
import com.example.InventoryManagementSystem.dto.ServiceMasterResponseDTO;
import com.example.InventoryManagementSystem.exception.ResourceNotFoundException;
import com.example.InventoryManagementSystem.model.ServiceMaster;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ServiceMasterServiceImpl implements ServiceMasterService {

    private final ServiceMasterRepository repository;

    @Override
    public ServiceMasterResponseDTO createService(ServiceMasterRequestDTO dto) {
        ServiceMaster service = new ServiceMaster();
        service.setServiceCode(dto.getServiceCode());
        service.setServiceName(dto.getServiceName());
        service.setDescription(dto.getDescription());
        service.setDefaultPrice(dto.getDefaultPrice());
        if (dto.getGstPercentage() != null) service.setGstPercentage(dto.getGstPercentage());
        service.setDurationMinutes(dto.getDurationMinutes());
        service.setStatus(dto.getStatus() != null ? dto.getStatus() : "active");

        return mapToDTO(repository.save(service));
    }

    @Override
    public ServiceMasterResponseDTO getServiceById(Long id) {
        ServiceMaster service = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Service not found with id: " + id));
        return mapToDTO(service);
    }

    @Override
    public List<ServiceMasterResponseDTO> getAllServices() {
        return repository.findAll().stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Override
    public ServiceMasterResponseDTO updateService(Long id, ServiceMasterRequestDTO dto) {
        ServiceMaster service = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Service not found with id: " + id));

        if (dto.getServiceCode() != null) service.setServiceCode(dto.getServiceCode());
        if (dto.getServiceName() != null) service.setServiceName(dto.getServiceName());
        if (dto.getDescription() != null) service.setDescription(dto.getDescription());
        if (dto.getDefaultPrice() != null) service.setDefaultPrice(dto.getDefaultPrice());
        if (dto.getGstPercentage() != null) service.setGstPercentage(dto.getGstPercentage());
        if (dto.getDurationMinutes() != null) service.setDurationMinutes(dto.getDurationMinutes());
        if (dto.getStatus() != null) service.setStatus(dto.getStatus());

        return mapToDTO(repository.save(service));
    }

    @Override
    public void deleteService(Long id) {
        ServiceMaster service = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Service not found with id: " + id));
        repository.delete(service);
    }

    private ServiceMasterResponseDTO mapToDTO(ServiceMaster service) {
        ServiceMasterResponseDTO dto = new ServiceMasterResponseDTO();
        dto.setServiceId(service.getServiceId());
        dto.setServiceCode(service.getServiceCode());
        dto.setServiceName(service.getServiceName());
        dto.setDescription(service.getDescription());
        dto.setDefaultPrice(service.getDefaultPrice());
        dto.setGstPercentage(service.getGstPercentage());
        dto.setDurationMinutes(service.getDurationMinutes());
        dto.setStatus(service.getStatus());
        dto.setCreatedAt(service.getCreatedAt());
        dto.setUpdatedAt(service.getUpdatedAt());
        return dto;
    }
}
