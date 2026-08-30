package com.example.InventoryManagementSystem.service;

import com.example.InventoryManagementSystem.Repository.LeaveRequestRepository;
import com.example.InventoryManagementSystem.Repository.UserRepository;
import com.example.InventoryManagementSystem.dto.LeaveRequestRequestDTO;
import com.example.InventoryManagementSystem.dto.LeaveRequestResponseDTO;
import com.example.InventoryManagementSystem.exception.ResourceNotFoundException;
import com.example.InventoryManagementSystem.model.LeaveRequest;
import com.example.InventoryManagementSystem.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LeaveRequestServiceImpl implements LeaveRequestService {

    private static final List<String> VALID_TYPES = List.of("PAID", "UNPAID");

    private final LeaveRequestRepository repository;
    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;
    private final AuditLogService auditLogService;

    @Override
    public LeaveRequestResponseDTO create(LeaveRequestRequestDTO dto) {
        userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + dto.getUserId()));
        if (dto.getLeaveType() == null || !VALID_TYPES.contains(dto.getLeaveType().toUpperCase())) {
            throw new IllegalArgumentException("Invalid leave type: " + dto.getLeaveType());
        }
        if (dto.getEndDate().isBefore(dto.getStartDate())) {
            throw new IllegalArgumentException("endDate cannot be before startDate");
        }

        LeaveRequest leave = LeaveRequest.builder()
                .userId(dto.getUserId())
                .leaveType(dto.getLeaveType().toUpperCase())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .reason(dto.getReason())
                .status("PENDING")
                .build();

        LeaveRequest saved = repository.save(leave);
        auditLogService.record("LEAVE_REQUESTED", "LEAVE_REQUEST", saved.getLeaveId(),
                saved.getLeaveType() + " leave requested for user #" + saved.getUserId()
                        + " (" + saved.getStartDate() + " to " + saved.getEndDate() + ").");
        return mapToDto(saved);
    }

    @Override
    public LeaveRequestResponseDTO getById(Long id) {
        return mapToDto(repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Leave request not found with id: " + id)));
    }

    @Override
    public List<LeaveRequestResponseDTO> getAll() {
        return repository.findAll().stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Override
    public List<LeaveRequestResponseDTO> getByUserId(Long userId) {
        return repository.findByUserId(userId).stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Override
    public LeaveRequestResponseDTO update(Long id, LeaveRequestRequestDTO dto) {
        LeaveRequest leave = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Leave request not found with id: " + id));
        // Once payroll may already have consumed this leave (APPROVED), only status changes
        // (approve/reject endpoints) should touch it — editing dates/type after the fact would
        // silently desync it from any already-generated payroll snapshot.
        if ("APPROVED".equals(leave.getStatus())) {
            throw new IllegalArgumentException("Cannot edit an already-approved leave request — reject and create a new one instead");
        }
        if (dto.getLeaveType() != null) {
            if (!VALID_TYPES.contains(dto.getLeaveType().toUpperCase())) {
                throw new IllegalArgumentException("Invalid leave type: " + dto.getLeaveType());
            }
            leave.setLeaveType(dto.getLeaveType().toUpperCase());
        }
        if (dto.getStartDate() != null) leave.setStartDate(dto.getStartDate());
        if (dto.getEndDate() != null) leave.setEndDate(dto.getEndDate());
        if (leave.getEndDate().isBefore(leave.getStartDate())) {
            throw new IllegalArgumentException("endDate cannot be before startDate");
        }
        if (dto.getReason() != null) leave.setReason(dto.getReason());
        return mapToDto(repository.save(leave));
    }

    @Override
    public LeaveRequestResponseDTO approve(Long id) {
        LeaveRequest leave = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Leave request not found with id: " + id));
        if (!"PENDING".equals(leave.getStatus())) {
            throw new IllegalArgumentException("Only a PENDING leave request can be approved");
        }
        leave.setStatus("APPROVED");
        leave.setApprovedByUserId(currentUserService.getCurrentUserId());
        leave.setApprovedAt(OffsetDateTime.now());
        LeaveRequest saved = repository.save(leave);
        auditLogService.record("LEAVE_APPROVED", "LEAVE_REQUEST", saved.getLeaveId(),
                "Leave request #" + saved.getLeaveId() + " approved for user #" + saved.getUserId() + ".");
        return mapToDto(saved);
    }

    @Override
    public LeaveRequestResponseDTO reject(Long id) {
        LeaveRequest leave = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Leave request not found with id: " + id));
        if (!"PENDING".equals(leave.getStatus())) {
            throw new IllegalArgumentException("Only a PENDING leave request can be rejected");
        }
        leave.setStatus("REJECTED");
        leave.setApprovedByUserId(currentUserService.getCurrentUserId());
        leave.setApprovedAt(OffsetDateTime.now());
        LeaveRequest saved = repository.save(leave);
        auditLogService.record("LEAVE_REJECTED", "LEAVE_REQUEST", saved.getLeaveId(),
                "Leave request #" + saved.getLeaveId() + " rejected for user #" + saved.getUserId() + ".");
        return mapToDto(saved);
    }

    @Override
    public void delete(Long id) {
        LeaveRequest leave = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Leave request not found with id: " + id));
        repository.delete(leave);
    }

    private LeaveRequestResponseDTO mapToDto(LeaveRequest leave) {
        LeaveRequestResponseDTO dto = new LeaveRequestResponseDTO();
        dto.setLeaveId(leave.getLeaveId());
        dto.setUserId(leave.getUserId());
        dto.setLeaveType(leave.getLeaveType());
        dto.setStartDate(leave.getStartDate());
        dto.setEndDate(leave.getEndDate());
        dto.setNumberOfDays((int) (ChronoUnit.DAYS.between(leave.getStartDate(), leave.getEndDate()) + 1));
        dto.setReason(leave.getReason());
        dto.setStatus(leave.getStatus());
        dto.setApprovedByUserId(leave.getApprovedByUserId());
        dto.setApprovedAt(leave.getApprovedAt());
        dto.setCreatedAt(leave.getCreatedAt());
        dto.setUpdatedAt(leave.getUpdatedAt());

        userRepository.findById(leave.getUserId()).ifPresent(u -> dto.setUserName(displayName(u)));
        if (leave.getApprovedByUserId() != null) {
            userRepository.findById(leave.getApprovedByUserId()).ifPresent(u -> dto.setApprovedByName(displayName(u)));
        }
        return dto;
    }

    private String displayName(User user) {
        if (user.getFullName() != null && !user.getFullName().isBlank()) return user.getFullName();
        return user.getUsername();
    }
}
