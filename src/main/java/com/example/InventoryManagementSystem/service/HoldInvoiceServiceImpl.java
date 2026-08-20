package com.example.InventoryManagementSystem.service;

import com.example.InventoryManagementSystem.Repository.CustomerRepository;
import com.example.InventoryManagementSystem.Repository.HoldInvoiceRepository;
import com.example.InventoryManagementSystem.Repository.VehicleRepository;
import com.example.InventoryManagementSystem.dto.HoldInvoiceRequestDto;
import com.example.InventoryManagementSystem.dto.HoldInvoiceResponseDto;
import com.example.InventoryManagementSystem.model.HoldInvoice;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HoldInvoiceServiceImpl
        implements HoldInvoiceService {

    private final HoldInvoiceRepository holdInvoiceRepository;
    private final CustomerRepository customerRepository;
    private final VehicleRepository vehicleRepository;

    // CREATE
    @Override
    public HoldInvoiceResponseDto createHoldInvoice(
            HoldInvoiceRequestDto dto) {

        HoldInvoice holdInvoice = new HoldInvoice();
        holdInvoice.setCustomerId(dto.getCustomerId());
        holdInvoice.setVehicleId(dto.getVehicleId());
        holdInvoice.setStatus(dto.getStatus() != null ? dto.getStatus() : "HELD");
        holdInvoice.setData(dto.getData());

        HoldInvoice saved =
                holdInvoiceRepository.save(
                        holdInvoice);

        return mapToDto(saved);
    }

    // GET ALL
    @Override
    public List<HoldInvoiceResponseDto>
    getAllHoldInvoices() {

        return holdInvoiceRepository.findAll()
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    // GET BY ID
    @Override
    public HoldInvoiceResponseDto
    getHoldInvoiceById(Long id) {

        HoldInvoice holdInvoice =
                holdInvoiceRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Hold invoice not found"));

        return mapToDto(holdInvoice);
    }

    @Override
    public HoldInvoiceResponseDto updateHoldInvoice(
            Long id,
            HoldInvoiceRequestDto dto) {

        HoldInvoice holdInvoice =
                holdInvoiceRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Hold invoice not found"));

        if (dto.getCustomerId() != null) holdInvoice.setCustomerId(dto.getCustomerId());
        if (dto.getVehicleId() != null) holdInvoice.setVehicleId(dto.getVehicleId());
        if (dto.getStatus() != null) holdInvoice.setStatus(dto.getStatus());
        if (dto.getData() != null) holdInvoice.setData(dto.getData());

        HoldInvoice updated =
                holdInvoiceRepository.save(holdInvoice);

        return mapToDto(updated);
    }

    // DELETE
    @Override
    public void deleteHoldInvoice(Long id) {

        HoldInvoice holdInvoice =
                holdInvoiceRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Hold invoice not found"));

        holdInvoiceRepository.delete(
                holdInvoice);
    }

    // MAP ENTITY TO DTO
    private HoldInvoiceResponseDto mapToDto(
            HoldInvoice holdInvoice) {

        HoldInvoiceResponseDto.HoldInvoiceResponseDtoBuilder builder = HoldInvoiceResponseDto.builder()
                .holdId(holdInvoice.getHoldId())
                .customerId(holdInvoice.getCustomerId())
                .vehicleId(holdInvoice.getVehicleId())
                .status(holdInvoice.getStatus())
                .data(holdInvoice.getData())
                .createdAt(holdInvoice.getCreatedAt());

        if (holdInvoice.getCustomerId() != null) {
            customerRepository.findById(holdInvoice.getCustomerId())
                    .ifPresent(c -> builder.customerName(c.getCustomerName()));
        }
        if (holdInvoice.getVehicleId() != null) {
            vehicleRepository.findById(holdInvoice.getVehicleId()).ifPresent(v -> {
                builder.vehicleModel(v.getVehicleModel());
                builder.registrationNumber(v.getRegistrationNumber());
            });
        }

        return builder.build();
    }
}
