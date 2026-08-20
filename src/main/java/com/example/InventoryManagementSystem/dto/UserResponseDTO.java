package com.example.InventoryManagementSystem.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserResponseDTO {

    private Long userId;

    private String username;

    private String firstName;

    private String lastName;

    private String fullName;

    private String email;

    private String mobileNumber;

    private Integer roleId;

    private String status;

    private Boolean active;
}