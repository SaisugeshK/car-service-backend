package com.example.InventoryManagementSystem.Repository;

import com.example.InventoryManagementSystem.model.NotificationEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationEventRepository extends JpaRepository<NotificationEvent, Long> {

    List<NotificationEvent> findAllByOrderByCreatedAtDesc();

    long countByIsReadFalse();
}
