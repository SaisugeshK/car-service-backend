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
    private final ServiceReminderService serviceReminderService;

    @Override
    public VehicleResponseDTO createVehicle(VehicleRequestDTO dto) {

        customerRepository.findById(dto.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + dto.getCustomerId()));
        rejectIfRegistrationTaken(dto.getRegistrationNumber(), null);

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
        vehicle.setVehicleCategory(dto.getVehicleCategory());
        vehicle.setInsuranceCompany(dto.getInsuranceCompany());
        vehicle.setInsuranceExpiry(dto.getInsuranceExpiry());
        vehicle.setPucExpiry(dto.getPucExpiry());
        vehicle.setNotes(dto.getNotes());

        Vehicle saved = repository.save(vehicle);
        syncExpiryReminders(saved);
        return mapToDTO(saved);
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
        if (dto.getRegistrationNumber() != null) {
            rejectIfRegistrationTaken(dto.getRegistrationNumber(), id);
            vehicle.setRegistrationNumber(dto.getRegistrationNumber());
        }
        if (dto.getOdometer() != null) vehicle.setOdometer(dto.getOdometer());
        if (dto.getVehicleType() != null) vehicle.setVehicleType(dto.getVehicleType());
        if (dto.getFuelType() != null) vehicle.setFuelType(dto.getFuelType());
        if (dto.getColor() != null) vehicle.setColor(dto.getColor());
        if (dto.getYear() != null) vehicle.setYear(dto.getYear());
        if (dto.getChassisNumber() != null) vehicle.setChassisNumber(dto.getChassisNumber());
        if (dto.getEngineNumber() != null) vehicle.setEngineNumber(dto.getEngineNumber());
        if (dto.getVehicleCategory() != null) vehicle.setVehicleCategory(dto.getVehicleCategory());
        if (dto.getInsuranceCompany() != null) vehicle.setInsuranceCompany(dto.getInsuranceCompany());
        if (dto.getInsuranceExpiry() != null) vehicle.setInsuranceExpiry(dto.getInsuranceExpiry());
        if (dto.getPucExpiry() != null) vehicle.setPucExpiry(dto.getPucExpiry());
        if (dto.getNotes() != null) vehicle.setNotes(dto.getNotes());

        Vehicle saved = repository.save(vehicle);
        syncExpiryReminders(saved);
        return mapToDTO(saved);
    }

    // A registration plate is unique in the real world by definition — there is no legitimate
    // case for two vehicle records sharing one, and letting it happen means service history,
    // invoices, and reminders can silently split across "duplicate" vehicles for the same car.
    private void rejectIfRegistrationTaken(String registrationNumber, Long excludeVehicleId) {
        if (registrationNumber == null || registrationNumber.isBlank()) return;
        repository.findByRegistrationNumberIgnoreCase(registrationNumber.trim()).stream()
                .filter(existing -> excludeVehicleId == null || !existing.getVehicleId().equals(excludeVehicleId))
                .findFirst()
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("A vehicle with registration number '" + registrationNumber
                            + "' already exists (vehicle #" + existing.getVehicleId() + ")");
                });
    }

    // Keeps at most one live INSURANCE_EXPIRY and one live PUC_EXPIRY reminder per vehicle in
    // sync with whatever dates are currently on file — see
    // ServiceReminderServiceImpl.upsertAutoReminder for the dedup logic.
    private void syncExpiryReminders(Vehicle vehicle) {
        String vehicleLabel = (vehicle.getMake() != null ? vehicle.getMake() + " " : "") + vehicle.getVehicleModel()
                + " (" + vehicle.getRegistrationNumber() + ")";
        serviceReminderService.upsertAutoReminder(vehicle.getVehicleId(), "INSURANCE_EXPIRY",
                vehicle.getInsuranceExpiry(), null, "Insurance expiry for " + vehicleLabel);
        serviceReminderService.upsertAutoReminder(vehicle.getVehicleId(), "PUC_EXPIRY",
                vehicle.getPucExpiry(), null, "PUC expiry for " + vehicleLabel);
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
        dto.setVehicleCategory(vehicle.getVehicleCategory());
        dto.setInsuranceCompany(vehicle.getInsuranceCompany());
        dto.setInsuranceExpiry(vehicle.getInsuranceExpiry());
        dto.setPucExpiry(vehicle.getPucExpiry());
        dto.setNotes(vehicle.getNotes());
        dto.setCreatedAt(vehicle.getCreatedAt());
        dto.setUpdatedAt(vehicle.getUpdatedAt());

        customerRepository.findById(vehicle.getCustomerId())
                .ifPresent(c -> dto.setCustomerName(c.getCustomerName()));

        return dto;
    }
}
