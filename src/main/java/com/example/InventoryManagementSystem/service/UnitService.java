package com.example.InventoryManagementSystem.service;

import com.example.InventoryManagementSystem.dto.UnitRequestDto;
import com.example.InventoryManagementSystem.dto.UnitResponseDto;

import java.util.List;

public interface UnitService {

    UnitResponseDto createUnit(
            UnitRequestDto dto);

    List<UnitResponseDto> getAllUnits();

    UnitResponseDto getUnitById(Long id);

    UnitResponseDto updateUnit(
            Long id,
            UnitRequestDto dto);

    void deleteUnit(Long id);
}