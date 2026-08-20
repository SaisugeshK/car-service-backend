package com.example.InventoryManagementSystem.service;

import com.example.InventoryManagementSystem.Repository.UnitRepository;
import com.example.InventoryManagementSystem.dto.UnitRequestDto;
import com.example.InventoryManagementSystem.dto.UnitResponseDto;
import com.example.InventoryManagementSystem.model.Unit;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UnitServiceImpl
        implements UnitService {

    private final UnitRepository unitRepository;

    // CREATE
    @Override
    public UnitResponseDto createUnit(
            UnitRequestDto dto) {

        Unit unit = new Unit();

        unit.setUnitName(
                dto.getUnitName());

        unit.setShortName(
                dto.getShortName());

        Unit saved =
                unitRepository.save(unit);

        return mapToDto(saved);
    }

    // GET ALL
    @Override
    public List<UnitResponseDto> getAllUnits() {

        return unitRepository.findAll()
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    // GET BY ID
    @Override
    public UnitResponseDto getUnitById(
            Long id) {

        Unit unit = unitRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Unit not found"));

        return mapToDto(unit);
    }

    // UPDATE
    @Override
    public UnitResponseDto updateUnit(
            Long id,
            UnitRequestDto dto) {

        Unit unit = unitRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Unit not found"));

        unit.setUnitName(
                dto.getUnitName());

        unit.setShortName(
                dto.getShortName());

        Unit updated =
                unitRepository.save(unit);

        return mapToDto(updated);
    }

    // DELETE
    @Override
    public void deleteUnit(Long id) {

        Unit unit = unitRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Unit not found"));

        unitRepository.delete(unit);
    }

    // MAP ENTITY TO DTO
    private UnitResponseDto mapToDto(
            Unit unit) {

        return new UnitResponseDto(
                unit.getUnitId(),
                unit.getUnitName(),
                unit.getShortName()
        );
    }
}