package com.example.InventoryManagementSystem.service;

import com.example.InventoryManagementSystem.Repository.CustomerRepository;
import com.example.InventoryManagementSystem.Repository.VehicleRepository;
import com.example.InventoryManagementSystem.dto.VehicleRequestDTO;
import com.example.InventoryManagementSystem.dto.VehicleResponseDTO;
import com.example.InventoryManagementSystem.exception.ResourceNotFoundException;
import com.example.InventoryManagementSystem.model.Vehicle;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VehicleServiceImpl implements VehicleService {

    private final VehicleRepository repository;
    private final CustomerRepository customerRepository;

    @Override
    public VehicleResponseDTO createVehicle(VehicleRequestDTO dto) {

        customerRepository.findById(dto.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + dto.getCustomerId()));

        Vehicle vehicle = new Vehicle();
        vehicle.setCustomerId(dto.getCustomerId());
        vehicle.setMake(dto.getMake());
        vehicle.setVehicleModel(dto.getVehicleModel());
        vehicle.setVariant(dto.getVariant());
        vehicle.setRegistrationNumber(dto.getRegistrationNumber());
        vehicle.setOdometer(dto.getOdometer());
        vehicle.setVehicleType(dto.getVehicleType());
        vehicle.setFuelType(dto.getFuelType());
        vehicle.setColor(dto.getColor());
        vehicle.setYear(dto.getYear());
        vehicle.setChassisNumber(dto.getChassisNumber());
        vehicle.setEngineNumber(dto.getEngineNumber());
        vehicle.setNotes(dto.getNotes());

        return mapToDTO(repository.save(vehicle));
    }

    @Override
    public VehicleResponseDTO getVehicleById(Long id) {
        Vehicle vehicle = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with id: " + id));
        return mapToDTO(vehicle);
    }

    @Override
    public List<VehicleResponseDTO> getAllVehicles() {
        return repository.findAll().stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Override
    public List<VehicleResponseDTO> getVehiclesByCustomerId(Long customerId) {
        return repository.findByCustomerId(customerId).stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Override
    public VehicleResponseDTO updateVehicle(Long id, VehicleRequestDTO dto) {
        Vehicle vehicle = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with id: " + id));

        if (dto.getCustomerId() != null) {
            customerRepository.findById(dto.getCustomerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + dto.getCustomerId()));
            vehicle.setCustomerId(dto.getCustomerId());
        }
        if (dto.getMake() != null) vehicle.setMake(dto.getMake());
        if (dto.getVehicleModel() != null) vehicle.setVehicleModel(dto.getVehicleModel());
        if (dto.getVariant() != null) vehicle.setVariant(dto.getVariant());
        if (dto.getRegistrationNumber() != null) vehicle.setRegistrationNumber(dto.getRegistrationNumber());
        if (dto.getOdometer() != null) vehicle.setOdometer(dto.getOdometer());
        if (dto.getVehicleType() != null) vehicle.setVehicleType(dto.getVehicleType());
        if (dto.getFuelType() != null) vehicle.setFuelType(dto.getFuelType());
        if (dto.getColor() != null) vehicle.setColor(dto.getColor());
        if (dto.getYear() != null) vehicle.setYear(dto.getYear());
        if (dto.getChassisNumber() != null) vehicle.setChassisNumber(dto.getChassisNumber());
        if (dto.getEngineNumber() != null) vehicle.setEngineNumber(dto.getEngineNumber());
        if (dto.getNotes() != null) vehicle.setNotes(dto.getNotes());

        return mapToDTO(repository.save(vehicle));
    }

    @Override
    public void deleteVehicle(Long id) {
        Vehicle vehicle = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with id: " + id));
        repository.delete(vehicle);
    }

    private VehicleResponseDTO mapToDTO(Vehicle vehicle) {
        VehicleResponseDTO dto = new VehicleResponseDTO();
        dto.setVehicleId(vehicle.getVehicleId());
        dto.setCustomerId(vehicle.getCustomerId());
        dto.setMake(vehicle.getMake());
        dto.setVehicleModel(vehicle.getVehicleModel());
        dto.setVariant(vehicle.getVariant());
        dto.setRegistrationNumber(vehicle.getRegistrationNumber());
        dto.setOdometer(vehicle.getOdometer());
        dto.setVehicleType(vehicle.getVehicleType());
        dto.setFuelType(vehicle.getFuelType());
        dto.setColor(vehicle.getColor());
        dto.setYear(vehicle.getYear());
        dto.setChassisNumber(vehicle.getChassisNumber());
        dto.setEngineNumber(vehicle.getEngineNumber());
        dto.setNotes(vehicle.getNotes());
        dto.setCreatedAt(vehicle.getCreatedAt());
        dto.setUpdatedAt(vehicle.getUpdatedAt());

        customerRepository.findById(vehicle.getCustomerId())
                .ifPresent(c -> dto.setCustomerName(c.getCustomerName()));

        return dto;
    }
}
