package com.example.InventoryManagementSystem.dto;

import lombok.Data;

@Data
public class UserRequestDTO {

    private String username;

    private String firstName;

    private String lastName;

    private String fullName;

    private String email;

    private String mobileNumber;

    private String password;

    private Integer roleId;

    private String status;

    private Boolean active;
}