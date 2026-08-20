package com.example.InventoryManagementSystem.service;

import com.example.InventoryManagementSystem.Repository.InspectionItemRepository;
import com.example.InventoryManagementSystem.dto.InspectionItemRequestDTO;
import com.example.InventoryManagementSystem.dto.InspectionItemResponseDTO;
import com.example.InventoryManagementSystem.exception.ResourceNotFoundException;
import com.example.InventoryManagementSystem.model.InspectionItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InspectionItemServiceImpl implements InspectionItemService {

    private final InspectionItemRepository repository;

    @Override
    public InspectionItemResponseDTO saveInspectionItem(InspectionItemRequestDTO dto) {
        InspectionItem item = repository
                .findByJobCardIdAndCategory(dto.getJobCardId(), dto.getCategory())
                .orElseGet(() -> {
                    InspectionItem i = new InspectionItem();
                    i.setJobCardId(dto.getJobCardId());
                    i.setCategory(dto.getCategory());
                    return i;
                });

        if (dto.getStatus() != null) item.setStatus(dto.getStatus());
        if (dto.getNotes() != null) item.setNotes(dto.getNotes());
        if (dto.getRecommendation() != null) item.setRecommendation(dto.getRecommendation());

        return mapToDto(repository.save(item));
    }

    @Override
    public List<InspectionItemResponseDTO> getByJobCardId(Long jobCardId) {
        return repository.findByJobCardId(jobCardId).stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Override
    public void deleteInspectionItem(Long id) {
        InspectionItem item = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inspection item not found with id: " + id));
        repository.delete(item);
    }

    private InspectionItemResponseDTO mapToDto(InspectionItem item) {
        InspectionItemResponseDTO dto = new InspectionItemResponseDTO();
        dto.setInspectionItemId(item.getInspectionItemId());
        dto.setJobCardId(item.getJobCardId());
        dto.setCategory(item.getCategory());
        dto.setStatus(item.getStatus());
        dto.setNotes(item.getNotes());
        dto.setRecommendation(item.getRecommendation());
        dto.setUpdatedAt(item.getUpdatedAt());
        return dto;
    }
}
