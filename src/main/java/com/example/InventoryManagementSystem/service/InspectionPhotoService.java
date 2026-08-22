package com.example.InventoryManagementSystem.service;

import com.example.InventoryManagementSystem.dto.InspectionPhotoResponseDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface InspectionPhotoService {

    InspectionPhotoResponseDTO upload(Long jobCardId, String category, MultipartFile file);

    List<InspectionPhotoResponseDTO> getByJobCard(Long jobCardId);

    StoredFile loadFile(Long photoId);

    void delete(Long photoId);

    // Small carrier for a file's bytes + the metadata needed to set response headers correctly —
    // not a JPA entity, just a return shape for the controller to stream from.
    record StoredFile(byte[] data, String contentType, String fileName) {
    }
}
