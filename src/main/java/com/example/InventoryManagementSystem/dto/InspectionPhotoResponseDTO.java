package com.example.InventoryManagementSystem.dto;

import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@Builder
public class InspectionPhotoResponseDTO {
    private Long inspectionPhotoId;
    private Long jobCardId;
    private String category;
    private String originalFileName;
    private String contentType;
    private Long fileSize;
    private OffsetDateTime uploadedAt;
    // Relative API path the frontend can use directly as an <img src>/download link —
    // GET is on the same authenticated /api/** surface as everything else, no separate
    // public static file server exposing photos without a token.
    private String url;
}
