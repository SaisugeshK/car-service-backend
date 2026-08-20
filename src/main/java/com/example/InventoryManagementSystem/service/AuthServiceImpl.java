package com.example.InventoryManagementSystem.service;

import com.example.InventoryManagementSystem.config.JwtUtil;
import com.example.InventoryManagementSystem.dto.AuthResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.InventoryManagementSystem.dto.LoginRequest;
import com.example.InventoryManagementSystem.dto.RegisterRequest;
import com.example.InventoryManagementSystem.model.User;
import com.example.InventoryManagementSystem.Repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    public String register(RegisterRequest request) {

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .build();

        userRepository.save(user);

        return "User Registered Successfully";
    }
    @Override
    public AuthResponse login(LoginRequest request) {

        User user = (User) userRepository.findByEmail(
                        request.getEmail())
                .orElseThrow(
                        () -> new RuntimeException(
                                "User Not Found"));

        if (!passwordEncoder.matches(
                request.getPassword(),
                user.getPasswordHash())) {

            throw new RuntimeException(
                    "Invalid Credentials");
        }

        String token =
                jwtUtil.generateToken(user.getEmail());

        return AuthResponse.builder()
                .token(token)
                .message("Login Successful")
                .build();
    }
}