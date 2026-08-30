package com.example.InventoryManagementSystem.service;

import com.example.InventoryManagementSystem.Repository.ServiceMasterRepository;
import com.example.InventoryManagementSystem.Repository.ServicePriceRepository;
import com.example.InventoryManagementSystem.dto.ServiceMasterRequestDTO;
import com.example.InventoryManagementSystem.dto.ServiceMasterResponseDTO;
import com.example.InventoryManagementSystem.dto.ServiceSizePriceDTO;
import com.example.InventoryManagementSystem.exception.ResourceNotFoundException;
import com.example.InventoryManagementSystem.model.ServiceMaster;
import com.example.InventoryManagementSystem.model.ServicePrice;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ServiceMasterServiceImpl implements ServiceMasterService {

    private final ServiceMasterRepository repository;
    private final ServicePriceRepository servicePriceRepository;

    @Override
    @Transactional
    public ServiceMasterResponseDTO createService(ServiceMasterRequestDTO dto) {
        ServiceMaster service = new ServiceMaster();
        service.setServiceCode(dto.getServiceCode());
        service.setServiceName(dto.getServiceName());
        service.setDescription(dto.getDescription());
        service.setDefaultPrice(dto.getDefaultPrice());
        if (dto.getGstPercentage() != null) service.setGstPercentage(dto.getGstPercentage());
        service.setDurationMinutes(dto.getDurationMinutes());
        service.setVehicleType(dto.getVehicleType());
        service.setStatus(dto.getStatus() != null ? dto.getStatus() : "active");

        ServiceMaster saved = repository.save(service);
        replaceSizePrices(saved.getServiceId(), dto.getSizePrices());
        return mapToDTO(saved);
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
    @Transactional
    public ServiceMasterResponseDTO updateService(Long id, ServiceMasterRequestDTO dto) {
        ServiceMaster service = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Service not found with id: " + id));

        if (dto.getServiceCode() != null) service.setServiceCode(dto.getServiceCode());
        if (dto.getServiceName() != null) service.setServiceName(dto.getServiceName());
        if (dto.getDescription() != null) service.setDescription(dto.getDescription());
        if (dto.getDefaultPrice() != null) service.setDefaultPrice(dto.getDefaultPrice());
        if (dto.getGstPercentage() != null) service.setGstPercentage(dto.getGstPercentage());
        if (dto.getDurationMinutes() != null) service.setDurationMinutes(dto.getDurationMinutes());
        if (dto.getVehicleType() != null) service.setVehicleType(dto.getVehicleType());
        if (dto.getStatus() != null) service.setStatus(dto.getStatus());

        ServiceMaster saved = repository.save(service);
        // sizePrices == null means "not part of this update"; an empty list means "clear them".
        if (dto.getSizePrices() != null) replaceSizePrices(id, dto.getSizePrices());
        return mapToDTO(saved);
    }

    @Override
    @Transactional
    public void deleteService(Long id) {
        ServiceMaster service = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Service not found with id: " + id));
        replaceSizePrices(id, null);
        repository.delete(service);
    }

    private void replaceSizePrices(Long serviceId, List<ServiceSizePriceDTO> sizePrices) {
        // Clear the old rows and make the removal hit the DB before the re-insert, otherwise
        // an unchanged (serviceId, sizeClassCode) pair collides with its own pending-delete row.
        List<ServicePrice> existing = servicePriceRepository.findByServiceId(serviceId);
        if (!existing.isEmpty()) {
            servicePriceRepository.deleteAllInBatch(existing);
            servicePriceRepository.flush();
        }
        if (sizePrices == null) return;
        for (ServiceSizePriceDTO sp : sizePrices) {
            if (sp.getSizeClassCode() == null || sp.getSizeClassCode().isBlank() || sp.getPrice() == null) continue;
            ServicePrice row = new ServicePrice();
            row.setServiceId(serviceId);
            row.setSizeClassCode(sp.getSizeClassCode().trim().toUpperCase());
            row.setPrice(sp.getPrice());
            servicePriceRepository.save(row);
        }
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
        dto.setVehicleType(service.getVehicleType());
        dto.setStatus(service.getStatus());
        dto.setSizePrices(servicePriceRepository.findByServiceId(service.getServiceId()).stream()
                .map(sp -> {
                    ServiceSizePriceDTO d = new ServiceSizePriceDTO();
                    d.setSizeClassCode(sp.getSizeClassCode());
                    d.setPrice(sp.getPrice());
                    return d;
                })
                .collect(Collectors.toList()));
        dto.setCreatedAt(service.getCreatedAt());
        dto.setUpdatedAt(service.getUpdatedAt());
        return dto;
    }
}
