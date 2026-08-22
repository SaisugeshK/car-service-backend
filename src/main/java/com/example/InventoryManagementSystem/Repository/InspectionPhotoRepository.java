package com.example.InventoryManagementSystem.Repository;

import com.example.InventoryManagementSystem.model.InspectionPhoto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InspectionPhotoRepository extends JpaRepository<InspectionPhoto, Long> {

    List<InspectionPhoto> findByJobCardIdOrderByUploadedAtDesc(Long jobCardId);
}
