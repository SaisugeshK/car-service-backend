package com.example.InventoryManagementSystem.service;

import com.example.InventoryManagementSystem.Repository.SettingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

// Phase 31 — a thin read-only helper over the existing generic key-value Settings store, so
// document-number prefixes (and anything else business logic needs to read) don't each duplicate
// the same findBySettingKey/fallback boilerplate. No new table — Settings already supports
// arbitrary keys; this just gives backend code outside SettingServiceImpl a clean way to read one.
@Service
@RequiredArgsConstructor
public class SettingsLookupService {

    private final SettingRepository repository;

    public String get(String key, String fallback) {
        return repository.findBySettingKey(key)
                .map(s -> s.getSettingValue() != null && !s.getSettingValue().isBlank() ? s.getSettingValue() : fallback)
                .orElse(fallback);
    }
}
