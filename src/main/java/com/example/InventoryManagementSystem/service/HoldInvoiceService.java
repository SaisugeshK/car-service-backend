package com.example.InventoryManagementSystem.service;

import com.example.InventoryManagementSystem.dto.HoldInvoiceRequestDto;
import com.example.InventoryManagementSystem.dto.HoldInvoiceResponseDto;

import java.util.List;

public interface HoldInvoiceService {

    HoldInvoiceResponseDto createHoldInvoice(
            HoldInvoiceRequestDto dto);

    List<HoldInvoiceResponseDto> getAllHoldInvoices();

    HoldInvoiceResponseDto updateHoldInvoice(
            Long id,
            HoldInvoiceRequestDto dto);

    HoldInvoiceResponseDto getHoldInvoiceById(
            Long id);

    void deleteHoldInvoice(Long id);
}