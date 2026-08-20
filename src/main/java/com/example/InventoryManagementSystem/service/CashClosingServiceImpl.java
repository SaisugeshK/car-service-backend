package com.example.InventoryManagementSystem.service;

import com.example.InventoryManagementSystem.Repository.BillingCounterRepository;
import com.example.InventoryManagementSystem.Repository.CashClosingRepository;
import com.example.InventoryManagementSystem.dto.CashClosingRequestDto;
import com.example.InventoryManagementSystem.dto.CashClosingResponseDto;
import com.example.InventoryManagementSystem.model.BillingCounter;
import com.example.InventoryManagementSystem.model.CashClosing;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CashClosingServiceImpl
        implements CashClosingService {

    private final CashClosingRepository cashClosingRepository;

    private final BillingCounterRepository billingCounterRepository;

    // CREATE
    @Override
    public CashClosingResponseDto createCashClosing(
            CashClosingRequestDto dto) {

        BillingCounter billingCounter =
                billingCounterRepository.findById(
                                dto.getCounterId())
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Counter not found"));

        CashClosing cashClosing =
                new CashClosing();

        cashClosing.setBillingCounter(
                billingCounter);

        cashClosing.setOpeningCash(
                dto.getOpeningCash());

        cashClosing.setClosingCash(
                dto.getClosingCash());

        cashClosing.setTotalSales(
                dto.getTotalSales());

        CashClosing saved =
                cashClosingRepository.save(
                        cashClosing);

        return mapToDto(saved);
    }

    // GET ALL
    @Override
    public List<CashClosingResponseDto>
    getAllCashClosings() {

        return cashClosingRepository.findAll()
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    // GET BY ID
    @Override
    public CashClosingResponseDto
    getCashClosingById(Long id) {

        CashClosing cashClosing =
                cashClosingRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Cash closing not found"));

        return mapToDto(cashClosing);
    }

    // UPDATE
    @Override
    public CashClosingResponseDto
    updateCashClosing(
            Long id,
            CashClosingRequestDto dto) {

        CashClosing cashClosing =
                cashClosingRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Cash closing not found"));

        BillingCounter billingCounter =
                billingCounterRepository.findById(
                                dto.getCounterId())
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Counter not found"));

        cashClosing.setBillingCounter(
                billingCounter);

        cashClosing.setOpeningCash(
                dto.getOpeningCash());

        cashClosing.setClosingCash(
                dto.getClosingCash());

        cashClosing.setTotalSales(
                dto.getTotalSales());

        CashClosing updated =
                cashClosingRepository.save(
                        cashClosing);

        return mapToDto(updated);
    }

    // DELETE
    @Override
    public void deleteCashClosing(Long id) {

        CashClosing cashClosing =
                cashClosingRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Cash closing not found"));

        cashClosingRepository.delete(
                cashClosing);
    }

    // MAP ENTITY TO DTO
    private CashClosingResponseDto mapToDto(
            CashClosing cashClosing) {

        return new CashClosingResponseDto(
                cashClosing.getClosingId(),
                cashClosing.getBillingCounter()
                        .getCounterId(),
                cashClosing.getBillingCounter()
                        .getCounterName(),
                cashClosing.getOpeningCash(),
                cashClosing.getClosingCash(),
                cashClosing.getTotalSales(),
                cashClosing.getCreatedAt()
        );
    }
}