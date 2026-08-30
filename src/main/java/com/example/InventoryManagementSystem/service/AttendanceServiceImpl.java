package com.example.InventoryManagementSystem.service;

import com.example.InventoryManagementSystem.Repository.AttendanceRepository;
import com.example.InventoryManagementSystem.Repository.UserRepository;
import com.example.InventoryManagementSystem.dto.AttendanceRequestDTO;
import com.example.InventoryManagementSystem.dto.AttendanceResponseDTO;
import com.example.InventoryManagementSystem.exception.ResourceNotFoundException;
import com.example.InventoryManagementSystem.model.Attendance;
import com.example.InventoryManagementSystem.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AttendanceServiceImpl implements AttendanceService {

    private static final List<String> VALID_STATUSES = List.of("PRESENT", "ABSENT", "WEEK_OFF", "HOLIDAY");

    private final AttendanceRepository repository;
    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;
    private final AuditLogService auditLogService;

    @Override
    public AttendanceResponseDTO create(AttendanceRequestDTO dto) {
        validate(dto);
        userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + dto.getUserId()));
        // Duplicate prevention (spec §7) — one attendance row per employee per day, backed by the
        // DB unique constraint too as the final safety net.
        if (repository.existsByUserIdAndAttendanceDate(dto.getUserId(), dto.getAttendanceDate())) {
            throw new IllegalArgumentException("Attendance for this employee on " + dto.getAttendanceDate() + " is already marked");
        }

        Attendance attendance = Attendance.builder()
                .userId(dto.getUserId())
                .attendanceDate(dto.getAttendanceDate())
                .status(dto.getStatus().toUpperCase())
                .markedByUserId(currentUserService.getCurrentUserId())
                .notes(dto.getNotes())
                .build();

        Attendance saved = repository.save(attendance);
        auditLogService.record("ATTENDANCE_MARKED", "ATTENDANCE", saved.getAttendanceId(),
                "Attendance marked " + saved.getStatus() + " for user #" + saved.getUserId() + " on " + saved.getAttendanceDate() + ".");
        return mapToDto(saved);
    }

    @Override
    public AttendanceResponseDTO getById(Long id) {
        return mapToDto(repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance record not found with id: " + id)));
    }

    @Override
    public List<AttendanceResponseDTO> getAll() {
        return repository.findAll().stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Override
    public List<AttendanceResponseDTO> getByUserId(Long userId) {
        return repository.findByUserId(userId).stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Override
    public AttendanceResponseDTO update(Long id, AttendanceRequestDTO dto) {
        Attendance attendance = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance record not found with id: " + id));
        if (dto.getStatus() != null) {
            if (!VALID_STATUSES.contains(dto.getStatus().toUpperCase())) {
                throw new IllegalArgumentException("Invalid attendance status: " + dto.getStatus());
            }
            attendance.setStatus(dto.getStatus().toUpperCase());
        }
        if (dto.getNotes() != null) attendance.setNotes(dto.getNotes());
        // userId/attendanceDate intentionally not editable here — that's a different record
        // (delete and re-create), same as this app's convention elsewhere for identity fields.
        return mapToDto(repository.save(attendance));
    }

    @Override
    public void delete(Long id) {
        Attendance attendance = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance record not found with id: " + id));
        repository.delete(attendance);
    }

    private void validate(AttendanceRequestDTO dto) {
        if (dto.getStatus() == null || !VALID_STATUSES.contains(dto.getStatus().toUpperCase())) {
            throw new IllegalArgumentException("Invalid attendance status: " + dto.getStatus());
        }
    }

    private AttendanceResponseDTO mapToDto(Attendance attendance) {
        AttendanceResponseDTO dto = new AttendanceResponseDTO();
        dto.setAttendanceId(attendance.getAttendanceId());
        dto.setUserId(attendance.getUserId());
        dto.setAttendanceDate(attendance.getAttendanceDate());
        dto.setStatus(attendance.getStatus());
        dto.setMarkedByUserId(attendance.getMarkedByUserId());
        dto.setNotes(attendance.getNotes());
        dto.setCreatedAt(attendance.getCreatedAt());
        dto.setUpdatedAt(attendance.getUpdatedAt());

        userRepository.findById(attendance.getUserId()).ifPresent(u -> dto.setUserName(displayName(u)));
        if (attendance.getMarkedByUserId() != null) {
            userRepository.findById(attendance.getMarkedByUserId()).ifPresent(u -> dto.setMarkedByName(displayName(u)));
        }
        return dto;
    }

    private String displayName(User user) {
        if (user.getFullName() != null && !user.getFullName().isBlank()) return user.getFullName();
        return user.getUsername();
    }
}
