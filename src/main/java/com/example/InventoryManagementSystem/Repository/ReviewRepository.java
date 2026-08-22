package com.example.InventoryManagementSystem.Repository;

import com.example.InventoryManagementSystem.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    boolean existsByJobCardId(Long jobCardId);
}
