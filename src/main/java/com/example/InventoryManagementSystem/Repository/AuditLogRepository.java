package com.example.InventoryManagementSystem.Repository;

import com.example.InventoryManagementSystem.model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    List<AuditLog> findAllByOrderByCreatedAtDesc();

    List<AuditLog> findByEntityTypeOrderByCreatedAtDesc(String entityType);
}
