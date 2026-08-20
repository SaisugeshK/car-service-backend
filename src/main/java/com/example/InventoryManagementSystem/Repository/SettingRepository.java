package com.example.InventoryManagementSystem.Repository;

import com.example.InventoryManagementSystem.model.Setting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SettingRepository extends JpaRepository<Setting, Long> {

    Optional<Setting> findBySettingKey(String settingKey);

    boolean existsBySettingKey(String settingKey);
}