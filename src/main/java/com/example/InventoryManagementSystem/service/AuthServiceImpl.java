package com.example.InventoryManagementSystem.service;

import com.example.InventoryManagementSystem.config.JwtUtil;
import com.example.InventoryManagementSystem.dto.AuthResponse;
import com.example.InventoryManagementSystem.dto.RefreshResponse;
import com.example.InventoryManagementSystem.exception.ResourceNotFoundException;
import com.example.InventoryManagementSystem.model.RefreshToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.InventoryManagementSystem.dto.LoginRequest;
import com.example.InventoryManagementSystem.dto.RegisterRequest;
import com.example.InventoryManagementSystem.model.Role;
import com.example.InventoryManagementSystem.model.User;
import com.example.InventoryManagementSystem.Repository.RefreshTokenRepository;
import com.example.InventoryManagementSystem.Repository.RoleRepository;
import com.example.InventoryManagementSystem.Repository.UserRepository;

import lombok.RequiredArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    // Opaque (not a JWT) — a refresh token's only job is to be looked up server-side against
    // refresh_tokens, so there's nothing to gain from it being self-describing/signed the way
    // the short-lived access token is.
    private static final long REFRESH_TOKEN_VALIDITY_DAYS = 30;

    @Override
    public String register(RegisterRequest request) {

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        // Self-registration always lands as MANAGER (operational role) — SUPER_ADMIN
        // is only granted by an existing SUPER_ADMIN via the Users screen, never by signup.
        Integer defaultRoleId = roleRepository.findByRoleName("MANAGER")
                .map(Role::getRoleId)
                .orElse(null);

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .roleId(defaultRoleId)
                .status("ACTIVE")
                .active(true)
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

        // Best-effort role lookup — a user row with no role_id (or a role_id that no
        // longer exists) still logs in, just with no role claim / no elevated access.
        String roleName = user.getRoleId() != null
                ? roleRepository.findById(user.getRoleId()).map(Role::getRoleName).orElse(null)
                : null;

        String token =
                jwtUtil.generateToken(user.getEmail(), roleName);

        String refreshToken = issueRefreshToken(user.getUserId());

        return AuthResponse.builder()
                .token(token)
                .refreshToken(refreshToken)
                .message("Login Successful")
                .userId(user.getUserId())
                .username(user.getUsername())
                .email(user.getEmail())
                .roleId(user.getRoleId())
                .roleName(roleName)
                .build();
    }

    // Phase 33/34 — closes the flagged gap: axios.js's refresh flow was already fully coded,
    // dormant only because nothing ever issued a refresh token. Rotates on every use (old row
    // revoked, a fresh one issued) so a single refresh token is never reused indefinitely.
    @Override
    @Transactional
    public RefreshResponse refresh(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new IllegalArgumentException("Refresh token is required");
        }

        RefreshToken existing = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new IllegalArgumentException("Invalid refresh token"));

        if (Boolean.TRUE.equals(existing.getRevoked())) {
            throw new IllegalArgumentException("Refresh token has been revoked");
        }
        if (existing.getExpiresAt().isBefore(OffsetDateTime.now())) {
            throw new IllegalArgumentException("Refresh token has expired");
        }

        User user = userRepository.findById(existing.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found for this refresh token"));

        String roleName = user.getRoleId() != null
                ? roleRepository.findById(user.getRoleId()).map(Role::getRoleName).orElse(null)
                : null;

        String newAccessToken = jwtUtil.generateToken(user.getEmail(), roleName);

        // Rotate: this token is spent, never valid again, even if intercepted after use.
        existing.setRevoked(true);
        refreshTokenRepository.save(existing);
        String newRefreshToken = issueRefreshToken(user.getUserId());

        return RefreshResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .build();
    }

    @Override
    @Transactional
    public void logout(String refreshToken) {
        // Best-effort and silent: a missing/already-invalid token on logout isn't an error —
        // the user is getting logged out either way (the frontend clears local storage
        // regardless of this call's outcome), this just also closes the door server-side.
        if (refreshToken == null || refreshToken.isBlank()) return;
        refreshTokenRepository.findByToken(refreshToken).ifPresent(t -> {
            t.setRevoked(true);
            refreshTokenRepository.save(t);
        });
    }

    private String issueRefreshToken(Long userId) {
        String token = UUID.randomUUID().toString() + UUID.randomUUID().toString();
        RefreshToken refreshToken = RefreshToken.builder()
                .token(token)
                .userId(userId)
                .expiresAt(OffsetDateTime.now().plusDays(REFRESH_TOKEN_VALIDITY_DAYS))
                .revoked(false)
                .build();
        refreshTokenRepository.save(refreshToken);
        return token;
    }
}
