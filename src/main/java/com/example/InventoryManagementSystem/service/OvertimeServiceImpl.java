package com.example.InventoryManagementSystem.service;

import com.example.InventoryManagementSystem.Repository.OvertimeEntryRepository;
import com.example.InventoryManagementSystem.Repository.UserRepository;
import com.example.InventoryManagementSystem.dto.OvertimeRequestDTO;
import com.example.InventoryManagementSystem.dto.OvertimeResponseDTO;
import com.example.InventoryManagementSystem.exception.ResourceNotFoundException;
import com.example.InventoryManagementSystem.model.OvertimeEntry;
import com.example.InventoryManagementSystem.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OvertimeServiceImpl implements OvertimeService {

    private final OvertimeEntryRepository repository;
    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;
    private final AuditLogService auditLogService;

    @Override
    public OvertimeResponseDTO create(OvertimeRequestDTO dto) {
        userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + dto.getUserId()));
        if (dto.getHours().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("hours must be greater than zero");
        }
        if (dto.getRate().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("rate cannot be negative");
        }

        OvertimeEntry entry = OvertimeEntry.builder()
                .userId(dto.getUserId())
                .workDate(dto.getWorkDate())
                .hours(dto.getHours())
                .rate(dto.getRate())
                .amount(dto.getHours().multiply(dto.getRate()))
                .status("PENDING")
                .notes(dto.getNotes())
                .build();

        OvertimeEntry saved = repository.save(entry);
        auditLogService.record("OVERTIME_LOGGED", "OVERTIME_ENTRY", saved.getOvertimeId(),
                "Overtime logged for user #" + saved.getUserId() + " — " + saved.getHours() + "h x " + saved.getRate() + " on " + saved.getWorkDate() + ".");
        return mapToDto(saved);
    }

    @Override
    public OvertimeResponseDTO getById(Long id) {
        return mapToDto(repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Overtime entry not found with id: " + id)));
    }

    @Override
    public List<OvertimeResponseDTO> getAll() {
        return repository.findAll().stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Override
    public List<OvertimeResponseDTO> getByUserId(Long userId) {
        return repository.findByUserId(userId).stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Override
    public OvertimeResponseDTO update(Long id, OvertimeRequestDTO dto) {
        OvertimeEntry entry = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Overtime entry not found with id: " + id));
        // Same reasoning as LeaveRequest: once APPROVED, payroll may already have consumed this
        // entry's amount — editing it after the fact would desync it from that snapshot.
        if ("APPROVED".equals(entry.getStatus())) {
            throw new IllegalArgumentException("Cannot edit an already-approved overtime entry — reject and create a new one instead");
        }
        if (dto.getWorkDate() != null) entry.setWorkDate(dto.getWorkDate());
        if (dto.getHours() != null) {
            if (dto.getHours().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("hours must be greater than zero");
            }
            entry.setHours(dto.getHours());
        }
        if (dto.getRate() != null) {
            if (dto.getRate().compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("rate cannot be negative");
            }
            entry.setRate(dto.getRate());
        }
        entry.setAmount(entry.getHours().multiply(entry.getRate()));
        if (dto.getNotes() != null) entry.setNotes(dto.getNotes());
        return mapToDto(repository.save(entry));
    }

    @Override
    public OvertimeResponseDTO approve(Long id) {
        OvertimeEntry entry = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Overtime entry not found with id: " + id));
        if (!"PENDING".equals(entry.getStatus())) {
            throw new IllegalArgumentException("Only a PENDING overtime entry can be approved");
        }
        entry.setStatus("APPROVED");
        entry.setApprovedByUserId(currentUserService.getCurrentUserId());
        entry.setApprovedAt(OffsetDateTime.now());
        OvertimeEntry saved = repository.save(entry);
        auditLogService.record("OVERTIME_APPROVED", "OVERTIME_ENTRY", saved.getOvertimeId(),
                "Overtime entry #" + saved.getOvertimeId() + " approved for user #" + saved.getUserId() + ".");
        return mapToDto(saved);
    }

    @Override
    public OvertimeResponseDTO reject(Long id) {
        OvertimeEntry entry = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Overtime entry not found with id: " + id));
        if (!"PENDING".equals(entry.getStatus())) {
            throw new IllegalArgumentException("Only a PENDING overtime entry can be rejected");
        }
        entry.setStatus("REJECTED");
        entry.setApprovedByUserId(currentUserService.getCurrentUserId());
        entry.setApprovedAt(OffsetDateTime.now());
        OvertimeEntry saved = repository.save(entry);
        auditLogService.record("OVERTIME_REJECTED", "OVERTIME_ENTRY", saved.getOvertimeId(),
                "Overtime entry #" + saved.getOvertimeId() + " rejected for user #" + saved.getUserId() + ".");
        return mapToDto(saved);
    }

    @Override
    public void delete(Long id) {
        OvertimeEntry entry = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Overtime entry not found with id: " + id));
        repository.delete(entry);
    }

    private OvertimeResponseDTO mapToDto(OvertimeEntry entry) {
        OvertimeResponseDTO dto = new OvertimeResponseDTO();
        dto.setOvertimeId(entry.getOvertimeId());
        dto.setUserId(entry.getUserId());
        dto.setWorkDate(entry.getWorkDate());
        dto.setHours(entry.getHours());
        dto.setRate(entry.getRate());
        dto.setAmount(entry.getAmount());
        dto.setStatus(entry.getStatus());
        dto.setApprovedByUserId(entry.getApprovedByUserId());
        dto.setApprovedAt(entry.getApprovedAt());
        dto.setNotes(entry.getNotes());
        dto.setCreatedAt(entry.getCreatedAt());
        dto.setUpdatedAt(entry.getUpdatedAt());

        userRepository.findById(entry.getUserId()).ifPresent(u -> dto.setUserName(displayName(u)));
        if (entry.getApprovedByUserId() != null) {
            userRepository.findById(entry.getApprovedByUserId()).ifPresent(u -> dto.setApprovedByName(displayName(u)));
        }
        return dto;
    }

    private String displayName(User user) {
        if (user.getFullName() != null && !user.getFullName().isBlank()) return user.getFullName();
        return user.getUsername();
    }
}
