package com.example.InventoryManagementSystem.service;

import com.example.InventoryManagementSystem.Repository.InspectionPhotoRepository;
import com.example.InventoryManagementSystem.dto.InspectionPhotoResponseDTO;
import com.example.InventoryManagementSystem.exception.ResourceNotFoundException;
import com.example.InventoryManagementSystem.model.InspectionPhoto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InspectionPhotoServiceImpl implements InspectionPhotoService {

    private final InspectionPhotoRepository repository;

    @Value("${app.upload-dir}")
    private String uploadDir;

    private static final List<String> ALLOWED_TYPES = List.of("image/jpeg", "image/png", "image/webp", "image/gif");

    @Override
    public InspectionPhotoResponseDTO upload(Long jobCardId, String category, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("No file was uploaded");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException("Only JPEG, PNG, WEBP, or GIF images are allowed");
        }

        try {
            Path dir = Paths.get(uploadDir);
            Files.createDirectories(dir);

            String extension = extensionFor(contentType);
            String storedFileName = UUID.randomUUID() + extension;
            Path target = dir.resolve(storedFileName);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

            InspectionPhoto photo = InspectionPhoto.builder()
                    .jobCardId(jobCardId)
                    .category(category)
                    .storedFileName(storedFileName)
                    .originalFileName(file.getOriginalFilename())
                    .contentType(contentType)
                    .fileSize(file.getSize())
                    .build();

            return mapToDto(repository.save(photo));
        } catch (IOException e) {
            throw new UncheckedIOException("Could not save the uploaded photo", e);
        }
    }

    @Override
    public List<InspectionPhotoResponseDTO> getByJobCard(Long jobCardId) {
        return repository.findByJobCardIdOrderByUploadedAtDesc(jobCardId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public StoredFile loadFile(Long photoId) {
        InspectionPhoto photo = repository.findById(photoId)
                .orElseThrow(() -> new ResourceNotFoundException("Photo not found with id: " + photoId));
        try {
            byte[] data = Files.readAllBytes(Paths.get(uploadDir).resolve(photo.getStoredFileName()));
            return new StoredFile(data, photo.getContentType(), photo.getOriginalFileName());
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read the stored photo", e);
        }
    }

    @Override
    public void delete(Long photoId) {
        InspectionPhoto photo = repository.findById(photoId)
                .orElseThrow(() -> new ResourceNotFoundException("Photo not found with id: " + photoId));
        try {
            Files.deleteIfExists(Paths.get(uploadDir).resolve(photo.getStoredFileName()));
        } catch (IOException e) {
            // The DB row is the source of truth for "does this photo exist" from the API's
            // perspective — an orphaned file on disk after a failed delete is a cleanup issue,
            // not a reason to leave the photo listed as still attached to the job card.
        }
        repository.delete(photo);
    }

    private String extensionFor(String contentType) {
        return switch (contentType.toLowerCase()) {
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            case "image/gif" -> ".gif";
            default -> ".jpg";
        };
    }

    private InspectionPhotoResponseDTO mapToDto(InspectionPhoto photo) {
        return InspectionPhotoResponseDTO.builder()
                .inspectionPhotoId(photo.getInspectionPhotoId())
                .jobCardId(photo.getJobCardId())
                .category(photo.getCategory())
                .originalFileName(photo.getOriginalFileName())
                .contentType(photo.getContentType())
                .fileSize(photo.getFileSize())
                .uploadedAt(photo.getUploadedAt())
                .url("/api/inspection-items/photos/" + photo.getInspectionPhotoId())
                .build();
    }
}
