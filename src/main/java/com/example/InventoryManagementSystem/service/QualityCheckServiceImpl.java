package com.example.InventoryManagementSystem.service;

import com.example.InventoryManagementSystem.Repository.JobCardRepository;
import com.example.InventoryManagementSystem.Repository.QualityCheckRepository;
import com.example.InventoryManagementSystem.dto.QualityCheckRequestDTO;
import com.example.InventoryManagementSystem.dto.QualityCheckResponseDTO;
import com.example.InventoryManagementSystem.exception.ResourceNotFoundException;
import com.example.InventoryManagementSystem.model.JobCard;
import com.example.InventoryManagementSystem.model.QualityCheck;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QualityCheckServiceImpl implements QualityCheckService {

    private final QualityCheckRepository repository;
    private final JobCardRepository jobCardRepository;

    @Override
    @Transactional
    public QualityCheckResponseDTO recordCheck(QualityCheckRequestDTO dto) {
        String result = dto.getResult() != null ? dto.getResult().trim().toUpperCase() : "";
        if (!result.equals("PASS") && !result.equals("FAIL")) {
            throw new IllegalArgumentException("result must be PASS or FAIL");
        }

        JobCard jobCard = jobCardRepository.findById(dto.getJobCardId())
                .orElseThrow(() -> new ResourceNotFoundException("Job card not found with id: " + dto.getJobCardId()));

        QualityCheck check = new QualityCheck();
        check.setJobCardId(dto.getJobCardId());
        check.setChecklistJson(dto.getChecklistJson());
        check.setResult(result);
        check.setNotes(dto.getNotes());
        check.setCheckedBy(dto.getCheckedBy());

        QualityCheck saved = repository.save(check);

        jobCard.setStatus("PASS".equals(result) ? "READY_FOR_DELIVERY" : "IN_PROGRESS");
        jobCardRepository.save(jobCard);

        return mapToDto(saved);
    }

    @Override
    public List<QualityCheckResponseDTO> getByJobCardId(Long jobCardId) {
        return repository.findByJobCardId(jobCardId).stream().map(this::mapToDto).collect(Collectors.toList());
    }

    private QualityCheckResponseDTO mapToDto(QualityCheck check) {
        QualityCheckResponseDTO dto = new QualityCheckResponseDTO();
        dto.setQualityCheckId(check.getQualityCheckId());
        dto.setJobCardId(check.getJobCardId());
        dto.setChecklistJson(check.getChecklistJson());
        dto.setResult(check.getResult());
        dto.setNotes(check.getNotes());
        dto.setCheckedBy(check.getCheckedBy());
        dto.setCheckedAt(check.getCheckedAt());
        return dto;
    }
}
