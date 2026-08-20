package com.example.InventoryManagementSystem.service;

import com.example.InventoryManagementSystem.dto.BillingCounterDto;
import com.example.InventoryManagementSystem.model.BillingCounter;
import com.example.InventoryManagementSystem.Repository.BillingCounterRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BillingCounterServiceImpl implements BillingCounterService {

    @Autowired
    private BillingCounterRepository repository;

    @Override
    public BillingCounterDto createBillingCounter(BillingCounterDto dto) {

        BillingCounter counter = new BillingCounter();

        counter.setCounterName(dto.getCounterName());
        counter.setLocation(dto.getLocation());
        counter.setStatus(dto.getStatus());

        BillingCounter saved = repository.save(counter);

        dto.setCounterId(saved.getCounterId());

        return dto;
    }

    @Override
    public List<BillingCounterDto> getAllBillingCounters() {

        return repository.findAll().stream().map(counter -> {

            BillingCounterDto dto = new BillingCounterDto();

            dto.setCounterId(counter.getCounterId());
            dto.setCounterName(counter.getCounterName());
            dto.setLocation(counter.getLocation());
            dto.setStatus(counter.getStatus());

            return dto;

        }).collect(Collectors.toList());
    }

    @Override
    public BillingCounterDto getBillingCounterById(Long id) {

        BillingCounter counter = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Billing Counter not found"));

        BillingCounterDto dto = new BillingCounterDto();

        dto.setCounterId(counter.getCounterId());
        dto.setCounterName(counter.getCounterName());
        dto.setLocation(counter.getLocation());
        dto.setStatus(counter.getStatus());

        return dto;
    }

    @Override
    public BillingCounterDto updateBillingCounter(Long id, BillingCounterDto dto) {

        BillingCounter counter = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Billing Counter not found"));

        counter.setCounterName(dto.getCounterName());
        counter.setLocation(dto.getLocation());
        counter.setStatus(dto.getStatus());

        repository.save(counter);

        dto.setCounterId(counter.getCounterId());

        return dto;
    }

    @Override
    public void deleteBillingCounter(Long id) {

        repository.deleteById(id);
    }
}