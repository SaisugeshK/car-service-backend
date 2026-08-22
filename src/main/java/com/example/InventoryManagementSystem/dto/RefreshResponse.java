package com.example.InventoryManagementSystem.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// Field names match exactly what car-service-frontend/src/api/axios.js's response interceptor
// already reads (accessToken / refreshToken) — that code was written and wired before this
// endpoint existed, so the contract is dictated by the frontend, not invented fresh here.
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RefreshResponse {
    private String accessToken;
    private String refreshToken;
}
