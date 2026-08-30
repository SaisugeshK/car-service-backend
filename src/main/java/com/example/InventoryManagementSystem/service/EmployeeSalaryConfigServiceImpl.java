package com.example.InventoryManagementSystem.service;

import com.example.InventoryManagementSystem.Repository.EmployeeSalaryConfigRepository;
import com.example.InventoryManagementSystem.Repository.RoleRepository;
import com.example.InventoryManagementSystem.Repository.UserRepository;
import com.example.InventoryManagementSystem.dto.EmployeeSalaryConfigRequestDTO;
import com.example.InventoryManagementSystem.dto.EmployeeSalaryConfigResponseDTO;
import com.example.InventoryManagementSystem.exception.ResourceNotFoundException;
import com.example.InventoryManagementSystem.model.EmployeeSalaryConfig;
import com.example.InventoryManagementSystem.model.Role;
import com.example.InventoryManagementSystem.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmployeeSalaryConfigServiceImpl implements EmployeeSalaryConfigService {

    private final EmployeeSalaryConfigRepository repository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final AuditLogService auditLogService;

    @Override
    public EmployeeSalaryConfigResponseDTO create(EmployeeSalaryConfigRequestDTO dto) {
        userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + dto.getUserId()));
        validateAmounts(dto);
        // "duplicate current configuration rejected" (spec §5/§36) — the DB unique constraint on
        // user_id is the final safety net, this is the friendlier pre-check.
        if (repository.existsByUserIdAndActiveTrue(dto.getUserId())) {
            throw new IllegalArgumentException("An active salary configuration already exists for this employee — edit it instead, or deactivate it first");
        }

        EmployeeSalaryConfig config = EmployeeSalaryConfig.builder()
                .userId(dto.getUserId())
                .basicPay(dto.getBasicPay())
                .hra(dto.getHra() != null ? dto.getHra() : BigDecimal.ZERO)
                .otherAllowances(dto.getOtherAllowances() != null ? dto.getOtherAllowances() : BigDecimal.ZERO)
                .deductions(dto.getDeductions() != null ? dto.getDeductions() : BigDecimal.ZERO)
                .effectiveFrom(dto.getEffectiveFrom())
                .active(dto.getActive() == null || dto.getActive())
                .notes(dto.getNotes())
                .build();

        EmployeeSalaryConfig saved = repository.save(config);
        auditLogService.record("SALARY_CONFIG_CREATED", "EMPLOYEE_SALARY_CONFIG", saved.getSalaryConfigId(),
                "Salary configured for user #" + saved.getUserId() + ": basic=" + saved.getBasicPay()
                        + ", hra=" + saved.getHra() + ", otherAllowances=" + saved.getOtherAllowances()
                        + ", deductions=" + saved.getDeductions() + ".");
        return mapToDto(saved);
    }

    @Override
    public EmployeeSalaryConfigResponseDTO getById(Long id) {
        return mapToDto(repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Salary configuration not found with id: " + id)));
    }

    @Override
    public EmployeeSalaryConfigResponseDTO getByUserId(Long userId) {
        return mapToDto(repository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("No salary configuration found for user id: " + userId)));
    }

    @Override
    public List<EmployeeSalaryConfigResponseDTO> getAll() {
        return repository.findAll().stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Override
    public EmployeeSalaryConfigResponseDTO update(Long id, EmployeeSalaryConfigRequestDTO dto) {
        EmployeeSalaryConfig config = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Salary configuration not found with id: " + id));
        validateAmounts(dto);

        String before = "basic=" + config.getBasicPay() + ", hra=" + config.getHra()
                + ", otherAllowances=" + config.getOtherAllowances() + ", deductions=" + config.getDeductions();

        if (dto.getBasicPay() != null) config.setBasicPay(dto.getBasicPay());
        if (dto.getHra() != null) config.setHra(dto.getHra());
        if (dto.getOtherAllowances() != null) config.setOtherAllowances(dto.getOtherAllowances());
        if (dto.getDeductions() != null) config.setDeductions(dto.getDeductions());
        if (dto.getEffectiveFrom() != null) config.setEffectiveFrom(dto.getEffectiveFrom());
        if (dto.getActive() != null) config.setActive(dto.getActive());
        if (dto.getNotes() != null) config.setNotes(dto.getNotes());

        EmployeeSalaryConfig saved = repository.save(config);
        String after = "basic=" + saved.getBasicPay() + ", hra=" + saved.getHra()
                + ", otherAllowances=" + saved.getOtherAllowances() + ", deductions=" + saved.getDeductions();
        // Historical payroll is unaffected by this — PayrollCalculationService already snapshotted
        // whatever the config said at generation time onto each SalaryPayment row.
        auditLogService.record("SALARY_CONFIG_UPDATED", "EMPLOYEE_SALARY_CONFIG", saved.getSalaryConfigId(),
                "Salary configuration #" + saved.getSalaryConfigId() + " for user #" + saved.getUserId()
                        + " changed. Before: " + before + ". After: " + after + ".");
        return mapToDto(saved);
    }

    @Override
    public EmployeeSalaryConfigResponseDTO deactivate(Long id) {
        EmployeeSalaryConfig config = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Salary configuration not found with id: " + id));
        config.setActive(false);
        EmployeeSalaryConfig saved = repository.save(config);
        auditLogService.record("SALARY_CONFIG_DEACTIVATED", "EMPLOYEE_SALARY_CONFIG", saved.getSalaryConfigId(),
                "Salary configuration #" + saved.getSalaryConfigId() + " for user #" + saved.getUserId() + " deactivated — excluded from future payroll generation.");
        return mapToDto(saved);
    }

    private void validateAmounts(EmployeeSalaryConfigRequestDTO dto) {
        rejectNegative(dto.getBasicPay(), "basicPay");
        rejectNegative(dto.getHra(), "hra");
        rejectNegative(dto.getOtherAllowances(), "otherAllowances");
        rejectNegative(dto.getDeductions(), "deductions");
    }

    private void rejectNegative(BigDecimal value, String field) {
        if (value != null && value.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(field + " cannot be negative");
        }
    }

    private EmployeeSalaryConfigResponseDTO mapToDto(EmployeeSalaryConfig config) {
        EmployeeSalaryConfigResponseDTO dto = new EmployeeSalaryConfigResponseDTO();
        dto.setSalaryConfigId(config.getSalaryConfigId());
        dto.setUserId(config.getUserId());
        dto.setBasicPay(config.getBasicPay());
        dto.setHra(config.getHra());
        dto.setOtherAllowances(config.getOtherAllowances());
        dto.setDeductions(config.getDeductions());
        dto.setEffectiveFrom(config.getEffectiveFrom());
        dto.setActive(config.getActive());
        dto.setNotes(config.getNotes());
        dto.setCreatedAt(config.getCreatedAt());
        dto.setUpdatedAt(config.getUpdatedAt());

        userRepository.findById(config.getUserId()).ifPresent(u -> {
            dto.setUserName(displayName(u));
            // No designation/jobTitle field exists on User — roleName is the closest available
            // stand-in, shown on the salary screen and the payslip PDF.
            if (u.getRoleId() != null) {
                roleRepository.findById(u.getRoleId()).map(Role::getRoleName).ifPresent(dto::setRoleName);
            }
        });
        return dto;
    }

    private String displayName(User user) {
        if (user.getFullName() != null && !user.getFullName().isBlank()) return user.getFullName();
        return user.getUsername();
    }
}
