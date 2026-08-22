package com.example.InventoryManagementSystem.service;

import com.example.InventoryManagementSystem.dto.ReviewRequestDTO;
import com.example.InventoryManagementSystem.dto.ReviewResponseDTO;

import java.util.List;

public interface ReviewService {

    ReviewResponseDTO create(ReviewRequestDTO dto);

    ReviewResponseDTO update(Long id, ReviewRequestDTO dto);

    List<ReviewResponseDTO> getAll();

    ReviewResponseDTO getById(Long id);

    void delete(Long id);
}
