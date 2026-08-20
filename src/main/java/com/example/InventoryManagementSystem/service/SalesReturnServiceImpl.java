package com.example.InventoryManagementSystem.service;

import com.example.InventoryManagementSystem.Repository.SalesReturnRepository;
import com.example.InventoryManagementSystem.dto.SalesReturnRequestDTO;
import com.example.InventoryManagementSystem.dto.SalesReturnResponseDTO;
import com.example.InventoryManagementSystem.model.SalesReturn;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SalesReturnServiceImpl
        implements SalesReturnService {

    private final SalesReturnRepository salesReturnRepository;

    // CREATE
    @Override
    public SalesReturnResponseDTO createReturn(
            SalesReturnRequestDTO dto) {

        SalesReturn salesReturn = SalesReturn.builder()
                .salesItemId(dto.getSalesItemId())
                .saleId(dto.getSaleId())
                .invoiceId(dto.getInvoiceId())
                .invoiceItemId(dto.getInvoiceItemId())
                .customerId(dto.getCustomerId())
                .returnQuantity(dto.getReturnQuantity())
                .reason(dto.getReason())
                .notes(dto.getNotes())
                .totalAmount(dto.getTotalAmount())
                .refundStatus(dto.getRefundStatus())
                .returnDate(OffsetDateTime.now())
                .createdAt(OffsetDateTime.now())
                .build();

        SalesReturn saved =
                salesReturnRepository.save(salesReturn);

        return mapToDTO(saved);
    }

    // GET BY ID
    @Override
    public SalesReturnResponseDTO getById(Long id) {

        SalesReturn salesReturn =
                salesReturnRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Sales Return not found"));

        return mapToDTO(salesReturn);
    }

    // GET ALL
    @Override
    public List<SalesReturnResponseDTO> getAll() {

        return salesReturnRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // UPDATE
    @Override
    public SalesReturnResponseDTO updateReturn(
            Long id,
            SalesReturnRequestDTO dto) {

        SalesReturn salesReturn =
                salesReturnRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Sales Return not found"));

        salesReturn.setSalesItemId(
                dto.getSalesItemId());

        salesReturn.setSaleId(
                dto.getSaleId());

        salesReturn.setInvoiceId(
                dto.getInvoiceId());

        salesReturn.setInvoiceItemId(
                dto.getInvoiceItemId());

        salesReturn.setCustomerId(
                dto.getCustomerId());

        salesReturn.setReturnQuantity(
                dto.getReturnQuantity());

        salesReturn.setReason(
                dto.getReason());

        salesReturn.setNotes(
                dto.getNotes());

        salesReturn.setTotalAmount(
                dto.getTotalAmount());

        salesReturn.setRefundStatus(
                dto.getRefundStatus());

        SalesReturn updated =
                salesReturnRepository.save(salesReturn);

        return mapToDTO(updated);
    }

    // DELETE
    @Override
    public void delete(Long id) {

        SalesReturn salesReturn =
                salesReturnRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Sales Return not found"));

        salesReturnRepository.delete(salesReturn);
    }

    // DTO MAPPER
    private SalesReturnResponseDTO mapToDTO(
            SalesReturn salesReturn) {

        return SalesReturnResponseDTO.builder()
                .returnId(
                        salesReturn.getReturnId())
                .salesItemId(
                        salesReturn.getSalesItemId())
                .saleId(
                        salesReturn.getSaleId())
                .invoiceId(
                        salesReturn.getInvoiceId())
                .invoiceItemId(
                        salesReturn.getInvoiceItemId())
                .customerId(
                        salesReturn.getCustomerId())
                .returnQuantity(
                        salesReturn.getReturnQuantity())
                .reason(
                        salesReturn.getReason())
                .notes(
                        salesReturn.getNotes())
                .totalAmount(
                        salesReturn.getTotalAmount())
                .refundStatus(
                        salesReturn.getRefundStatus())
                .returnDate(
                        salesReturn.getReturnDate())
                .createdAt(
                        salesReturn.getCreatedAt())
                .build();
    }
}