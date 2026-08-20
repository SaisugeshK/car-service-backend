package com.example.InventoryManagementSystem.service;

import com.example.InventoryManagementSystem.dto.SettingRequest;
import com.example.InventoryManagementSystem.dto.SettingResponse;

import java.util.List;

public interface SettingService {

    SettingResponse createSetting(SettingRequest request);

    SettingResponse updateSetting(Long id, SettingRequest request);

    SettingResponse getSettingById(Long id);

    List<SettingResponse> getAllSettings();

    void deleteSetting(Long id);
}