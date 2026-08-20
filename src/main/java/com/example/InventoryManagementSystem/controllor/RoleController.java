package com.example.InventoryManagementSystem.controllor;

import com.example.InventoryManagementSystem.dto.RoleRequestDTO;
import com.example.InventoryManagementSystem.dto.RoleResponseDTO;
import com.example.InventoryManagementSystem.service.RoleService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @PostMapping
    public ResponseEntity<RoleResponseDTO> createRole(
            @RequestBody RoleRequestDTO request) {

        return ResponseEntity.ok(
                roleService.createRole(request));
    }

    @GetMapping
    public ResponseEntity<List<RoleResponseDTO>> getAllRoles() {

        return ResponseEntity.ok(
                roleService.getAllRoles());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RoleResponseDTO> getRoleById(
            @PathVariable Integer id) {

        return ResponseEntity.ok(
                roleService.getRoleById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RoleResponseDTO> updateRole(
            @PathVariable Integer id,
            @RequestBody RoleRequestDTO request) {

        return ResponseEntity.ok(
                roleService.updateRole(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteRole(
            @PathVariable Integer id) {

        roleService.deleteRole(id);

        return ResponseEntity.ok("Role deleted successfully");
    }
}