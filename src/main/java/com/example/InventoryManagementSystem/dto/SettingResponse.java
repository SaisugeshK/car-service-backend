package com.example.InventoryManagementSystem.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class SettingResponse {

    private Long settingId;

    private String settingKey;

    private String settingValue;

    private LocalDateTime updatedAt;
}