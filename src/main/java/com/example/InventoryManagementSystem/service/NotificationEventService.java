package com.example.InventoryManagementSystem.service;

import com.example.InventoryManagementSystem.dto.NotificationEventResponseDTO;

import java.util.List;

public interface NotificationEventService {

    // Fire-and-forget creation used by every other service when a notification-worthy business
    // event happens (new customer, estimate approved, low stock, ...). referenceType/referenceId
    // may be null for events with nothing sensible to deep-link to.
    void raise(String type, String title, String message, String referenceType, Long referenceId);

    List<NotificationEventResponseDTO> getAll();

    long getUnreadCount();

    NotificationEventResponseDTO markRead(Long id);

    void markAllRead();
}
