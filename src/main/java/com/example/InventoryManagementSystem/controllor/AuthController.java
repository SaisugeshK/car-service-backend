package com.example.InventoryManagementSystem.controllor;

import com.example.InventoryManagementSystem.dto.AuthResponse;
import com.example.InventoryManagementSystem.dto.RefreshRequest;
import com.example.InventoryManagementSystem.dto.RefreshResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.InventoryManagementSystem.dto.LoginRequest;
import com.example.InventoryManagementSystem.dto.RegisterRequest;
import com.example.InventoryManagementSystem.service.AuthService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<String> register(
            @RequestBody RegisterRequest request) {

        return ResponseEntity.ok(
                authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @RequestBody LoginRequest request) {

        return ResponseEntity.ok(
                authService.login(request));
    }

    // Phase 33/34 — the endpoint car-service-frontend/src/api/axios.js was already calling
    // and reading (accessToken/refreshToken) whenever it existed.
    @PostMapping("/refresh")
    public ResponseEntity<RefreshResponse> refresh(
            @RequestBody RefreshRequest request) {

        return ResponseEntity.ok(
                authService.refresh(request.getRefreshToken()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @RequestBody(required = false) RefreshRequest request) {

        authService.logout(request != null ? request.getRefreshToken() : null);
        return ResponseEntity.ok().build();
    }
}