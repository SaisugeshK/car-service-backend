package com.example.InventoryManagementSystem.service;

import com.example.InventoryManagementSystem.dto.NotificationLogResponseDTO;
import com.example.InventoryManagementSystem.dto.NotificationSendRequestDTO;

import java.util.List;

public interface NotificationService {
    NotificationLogResponseDTO send(NotificationSendRequestDTO dto);
    List<NotificationLogResponseDTO> getByReference(String referenceType, Long referenceId);
    List<NotificationLogResponseDTO> getAll();
}
