package com.example.InventoryManagementSystem.service;

import com.example.InventoryManagementSystem.dto.RoleRequestDTO;
import com.example.InventoryManagementSystem.dto.RoleResponseDTO;

import java.util.List;

public interface RoleService {

    RoleResponseDTO createRole(RoleRequestDTO request);

    List<RoleResponseDTO> getAllRoles();

    RoleResponseDTO getRoleById(Integer roleId);

    RoleResponseDTO updateRole(Integer roleId, RoleRequestDTO request);

    void deleteRole(Integer roleId);
}