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
 * Phase 19 — roles didn't exist as data at all before this (0 rows in `roles`, every
 * existing user's role_id was null). Seeds the two roles the ERP spec requires and
 * backfills any user still missing a role, so the Users/Roles screens and the new
 * SUPER_ADMIN/MANAGER frontend gating have something real to point at. Idempotent —
 * safe to run on every startup.
 */
@Component
@RequiredArgsConstructor
public class RoleSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;

    @Override
    public void run(String... args) {
        Role superAdmin = ensureRole("SUPER_ADMIN", "Full access — revenue, financial reports, users, roles, settings, audit logs.");
        Role manager = ensureRole("MANAGER", "Operational access — customers, vehicles, jobs, billing, no owner-level financial/system data.");
        // HRM/payroll module — staff (technicians etc.) who need their own login to see only
        // their own payslips, with no access to any management screen. Never backfilled onto
        // existing roleless users below; an admin assigns it explicitly via the Users screen.
        ensureRole("EMPLOYEE", "Own payslip access only, no management access.");

        // Backfill pre-existing accounts that predate roles entirely. "admin" is the
        // original seeded account, so it becomes SUPER_ADMIN; any other roleless user
        // (e.g. the test@example.com account created earlier this session) defaults to
        // MANAGER, matching what self-registration now assigns going forward.
        for (User user : userRepository.findAll()) {
            if (user.getRoleId() != null) continue;
            boolean isOriginalAdmin = "admin".equalsIgnoreCase(user.getUsername());
            user.setRoleId(isOriginalAdmin ? superAdmin.getRoleId() : manager.getRoleId());
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
