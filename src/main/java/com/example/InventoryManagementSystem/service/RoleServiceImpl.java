package com.example.InventoryManagementSystem.service;

import com.example.InventoryManagementSystem.dto.RoleRequestDTO;
import com.example.InventoryManagementSystem.dto.RoleResponseDTO;
import com.example.InventoryManagementSystem.model.Role;
import com.example.InventoryManagementSystem.Repository.RoleRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;

    @Override
    public RoleResponseDTO createRole(RoleRequestDTO request) {

        if (roleRepository.existsByRoleName(request.getRoleName())) {
            throw new RuntimeException("Role already exists");
        }

        Role role = Role.builder()
                .roleName(request.getRoleName())
                .description(request.getDescription())
                .build();

        Role savedRole = roleRepository.save(role);

        return mapToResponse(savedRole);
    }

    @Override
    public List<RoleResponseDTO> getAllRoles() {

        return roleRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public RoleResponseDTO getRoleById(Integer roleId) {

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() ->
                        new RuntimeException("Role not found with id : " + roleId));

        return mapToResponse(role);
    }

    @Override
    public RoleResponseDTO updateRole(Integer roleId,
                                      RoleRequestDTO request) {

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() ->
                        new RuntimeException("Role not found with id : " + roleId));

        role.setRoleName(request.getRoleName());
        role.setDescription(request.getDescription());

        Role updatedRole = roleRepository.save(role);

        return mapToResponse(updatedRole);
    }

    @Override
    public void deleteRole(Integer roleId) {

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() ->
                        new RuntimeException("Role not found with id : " + roleId));

        roleRepository.delete(role);
    }

    private RoleResponseDTO mapToResponse(Role role) {

        return RoleResponseDTO.builder()
                .roleId(role.getRoleId())
                .roleName(role.getRoleName())
                .description(role.getDescription())
                .createdAt(role.getCreatedAt())
                .updatedAt(role.getUpdatedAt())
                .build();
    }
}