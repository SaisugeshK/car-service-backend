package com.example.InventoryManagementSystem.Repository;

import com.example.InventoryManagementSystem.model.NotificationLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationLogRepository extends JpaRepository<NotificationLog, Long> {

    List<NotificationLog> findByReferenceTypeAndReferenceIdOrderByCreatedAtDesc(String referenceType, Long referenceId);

    List<NotificationLog> findAllByOrderByCreatedAtDesc();
}
