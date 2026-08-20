package com.example.InventoryManagementSystem.service;

import com.example.InventoryManagementSystem.dto.AuthResponse;
import com.example.InventoryManagementSystem.dto.LoginRequest;
import com.example.InventoryManagementSystem.dto.RegisterRequest;

public interface AuthService {

    String register(RegisterRequest request);

    AuthResponse login(LoginRequest request);
}