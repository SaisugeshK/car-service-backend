package com.example.InventoryManagementSystem.dto;

import lombok.Data;

@Data
public class SettingRequest {

    private String settingKey;

    private String settingValue;
}