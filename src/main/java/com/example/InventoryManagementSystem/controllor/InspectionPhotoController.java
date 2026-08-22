package com.example.InventoryManagementSystem.controllor;

import com.example.InventoryManagementSystem.dto.InspectionPhotoResponseDTO;
import com.example.InventoryManagementSystem.service.InspectionPhotoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

// Phase 33/34 — closes the flagged MISSING BACKEND API gap: JobCardDetail.jsx's InspectionTab
// Photos section existed with an explicitly disabled upload button ("Not configured") rather
// than faking a save. Same /api/inspection-items base path as InspectionItemController — these
// are photos attached to that job card's inspection, not a separate concept — routed on distinct
// URL shapes so nothing here collides with the existing {id}/job-card/{jobCardId} mappings.
@RestController
@RequestMapping("/api/inspection-items")
@RequiredArgsConstructor
public class InspectionPhotoController {

    private final InspectionPhotoService service;

    @PostMapping(value = "/{jobCardId}/photos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<InspectionPhotoResponseDTO> upload(
            @PathVariable Long jobCardId,
            @RequestParam String category,
            @RequestParam("file") MultipartFile file) {

        return ResponseEntity.status(HttpStatus.CREATED).body(service.upload(jobCardId, category, file));
    }

    @GetMapping("/{jobCardId}/photos")
    public ResponseEntity<List<InspectionPhotoResponseDTO>> getByJobCard(@PathVariable Long jobCardId) {
        return ResponseEntity.ok(service.getByJobCard(jobCardId));
    }

    @GetMapping("/photos/{photoId}")
    public ResponseEntity<byte[]> getPhoto(@PathVariable Long photoId) {
        InspectionPhotoService.StoredFile file = service.loadFile(photoId);
        return ResponseEntity.ok()
                .contentType(file.contentType() != null ? MediaType.parseMediaType(file.contentType()) : MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + (file.fileName() != null ? file.fileName() : "photo") + "\"")
                .body(file.data());
    }

    @DeleteMapping("/photos/{photoId}")
    public ResponseEntity<String> delete(@PathVariable Long photoId) {
        service.delete(photoId);
        return ResponseEntity.ok("Photo deleted successfully");
    }
}
