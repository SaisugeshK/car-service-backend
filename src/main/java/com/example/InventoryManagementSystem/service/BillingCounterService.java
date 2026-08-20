package com.example.InventoryManagementSystem.service;

import com.example.InventoryManagementSystem.dto.BillingCounterDto;

import java.util.List;

public interface BillingCounterService {

    BillingCounterDto createBillingCounter(BillingCounterDto dto);

    List<BillingCounterDto> getAllBillingCounters();

    BillingCounterDto getBillingCounterById(Long id);

    BillingCounterDto updateBillingCounter(Long id, BillingCounterDto dto);

    void deleteBillingCounter(Long id);
}