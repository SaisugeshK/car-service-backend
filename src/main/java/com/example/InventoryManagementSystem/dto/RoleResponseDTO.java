package com.example.InventoryManagementSystem.dto;

import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@Builder
public class RoleResponseDTO {

    private Integer roleId;

    private String roleName;

    private String description;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;
}