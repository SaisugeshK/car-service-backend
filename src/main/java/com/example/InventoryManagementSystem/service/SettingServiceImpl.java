package com.example.InventoryManagementSystem.service;

import com.example.InventoryManagementSystem.dto.SettingRequest;
import com.example.InventoryManagementSystem.dto.SettingResponse;
import com.example.InventoryManagementSystem.model.Setting;
import com.example.InventoryManagementSystem.Repository.SettingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SettingServiceImpl implements SettingService {

    private final SettingRepository repository;

    @Override
    public SettingResponse createSetting(SettingRequest request) {

        if (repository.existsBySettingKey(request.getSettingKey())) {
            throw new RuntimeException("Setting Key Already Exists");
        }

        Setting setting = Setting.builder()
                .settingKey(request.getSettingKey())
                .settingValue(request.getSettingValue())
                .build();

        return mapToResponse(repository.save(setting));
    }

    @Override
    public SettingResponse updateSetting(Long id, SettingRequest request) {

        Setting setting = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Setting Not Found"));

        setting.setSettingKey(request.getSettingKey());
        setting.setSettingValue(request.getSettingValue());

        return mapToResponse(repository.save(setting));
    }

    @Override
    public SettingResponse getSettingById(Long id) {

        Setting setting = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Setting Not Found"));

        return mapToResponse(setting);
    }

    @Override
    public List<SettingResponse> getAllSettings() {

        return repository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public void deleteSetting(Long id) {

        Setting setting = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Setting Not Found"));

        repository.delete(setting);
    }

    private SettingResponse mapToResponse(Setting setting) {

        return SettingResponse.builder()
                .settingId(setting.getSettingId())
                .settingKey(setting.getSettingKey())
                .settingValue(setting.getSettingValue())
                .updatedAt(setting.getUpdatedAt())
                .build();
    }
}