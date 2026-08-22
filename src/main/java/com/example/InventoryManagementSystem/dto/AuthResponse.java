package com.example.InventoryManagementSystem.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {

    private String token;
    private String refreshToken;
    private String message;

    private Long userId;
    private String username;
    private String email;
    private Integer roleId;
    private String roleName;
}
