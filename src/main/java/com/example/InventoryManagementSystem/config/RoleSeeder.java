package com.example.InventoryManagementSystem.config;

import com.example.InventoryManagementSystem.Repository.RoleRepository;
import com.example.InventoryManagementSystem.Repository.UserRepository;
import com.example.InventoryManagementSystem.model.Role;
import com.example.InventoryManagementSystem.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

/**
 * The app has exactly two roles: SUPER_ADMIN (read/write on every module) and EMPLOYEE
 * (read-only, own payslips and own attendance). Seeds both and gives any user without a
 * valid role the least-privileged one. Idempotent — safe to run on every startup.
 */
@Component
@RequiredArgsConstructor
public class RoleSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;

    @Override
    public void run(String... args) {
        Role superAdmin = ensureRole("SUPER_ADMIN", "Full access — every module, read and write.");
        Role employee = ensureRole("EMPLOYEE", "Read-only access to own payslips and own attendance.");

        // A user with no role, or pointing at a role that no longer exists (e.g. the retired
        // MANAGER), gets EMPLOYEE. "admin" is the original seeded account, so it becomes
        // SUPER_ADMIN instead.
        for (User user : userRepository.findAll()) {
            if (user.getRoleId() != null && roleRepository.existsById(user.getRoleId())) continue;
            boolean isOriginalAdmin = "admin".equalsIgnoreCase(user.getUsername());
            user.setRoleId(isOriginalAdmin ? superAdmin.getRoleId() : employee.getRoleId());
            userRepository.save(user);
        }
    }

    private Role ensureRole(String name, String description) {
        return roleRepository.findByRoleName(name).orElseGet(() -> {
            Role role = Role.builder()
                    .roleName(name)
                    .description(description)
                    .createdAt(OffsetDateTime.now())
                    .updatedAt(OffsetDateTime.now())
                    .build();
            return roleRepository.save(role);
        });
    }
}
