package com.example.InventoryManagementSystem.service;

import com.example.InventoryManagementSystem.Repository.CustomerRepository;
import com.example.InventoryManagementSystem.Repository.InvoiceRepository;
import com.example.InventoryManagementSystem.Repository.JobCardRepository;
import com.example.InventoryManagementSystem.Repository.ReviewRepository;
import com.example.InventoryManagementSystem.Repository.VehicleRepository;
import com.example.InventoryManagementSystem.dto.ReviewRequestDTO;
import com.example.InventoryManagementSystem.dto.ReviewResponseDTO;
import com.example.InventoryManagementSystem.exception.ResourceNotFoundException;
import com.example.InventoryManagementSystem.model.Review;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository repository;
    private final CustomerRepository customerRepository;
    private final VehicleRepository vehicleRepository;
    private final JobCardRepository jobCardRepository;
    private final InvoiceRepository invoiceRepository;
    private final NotificationEventService notificationEventService;

    @Override
    public ReviewResponseDTO create(ReviewRequestDTO dto) {
        customerRepository.findById(dto.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + dto.getCustomerId()));

        // One review per completed job — a second submission for the same job card is almost
        // always a duplicate send, not a genuine second review.
        if (dto.getJobCardId() != null && repository.existsByJobCardId(dto.getJobCardId())) {
            throw new IllegalArgumentException("A review has already been recorded for this job card");
        }

        Review review = new Review();
        review.setCustomerId(dto.getCustomerId());
        review.setVehicleId(dto.getVehicleId());
        review.setJobCardId(dto.getJobCardId());
        review.setInvoiceId(dto.getInvoiceId());
        review.setRating(dto.getRating());
        review.setServiceQualityRating(dto.getServiceQualityRating());
        review.setStaffBehaviorRating(dto.getStaffBehaviorRating());
        review.setServiceTimeRating(dto.getServiceTimeRating());
        review.setPriceSatisfactionRating(dto.getPriceSatisfactionRating());
        review.setComment(dto.getComment());

        Review saved = repository.save(review);
        String ratingTone = saved.getRating() != null && saved.getRating() <= 2 ? " (low rating — worth a look)" : "";
        notificationEventService.raise("REVIEW", "New review submitted",
                "A " + saved.getRating() + "-star review was submitted." + ratingTone, "REVIEW", saved.getReviewId());
        return mapToDto(saved);
    }

    @Override
    public ReviewResponseDTO update(Long id, ReviewRequestDTO dto) {
        Review review = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with id: " + id));

        // Job-card linkage is fixed at creation — editing a review corrects the rating/comment,
        // it never reassigns which job it's about.
        if (dto.getRating() != null) review.setRating(dto.getRating());
        review.setServiceQualityRating(dto.getServiceQualityRating());
        review.setStaffBehaviorRating(dto.getStaffBehaviorRating());
        review.setServiceTimeRating(dto.getServiceTimeRating());
        review.setPriceSatisfactionRating(dto.getPriceSatisfactionRating());
        if (dto.getComment() != null) review.setComment(dto.getComment());

        return mapToDto(repository.save(review));
    }

    @Override
    public List<ReviewResponseDTO> getAll() {
        return repository.findAll().stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Override
    public ReviewResponseDTO getById(Long id) {
        Review review = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with id: " + id));
        return mapToDto(review);
    }

    @Override
    public void delete(Long id) {
        Review review = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with id: " + id));
        repository.delete(review);
    }

    private ReviewResponseDTO mapToDto(Review review) {
        ReviewResponseDTO dto = new ReviewResponseDTO();
        dto.setReviewId(review.getReviewId());
        dto.setCustomerId(review.getCustomerId());
        dto.setVehicleId(review.getVehicleId());
        dto.setJobCardId(review.getJobCardId());
        dto.setInvoiceId(review.getInvoiceId());
        dto.setRating(review.getRating());
        dto.setServiceQualityRating(review.getServiceQualityRating());
        dto.setStaffBehaviorRating(review.getStaffBehaviorRating());
        dto.setServiceTimeRating(review.getServiceTimeRating());
        dto.setPriceSatisfactionRating(review.getPriceSatisfactionRating());
        dto.setComment(review.getComment());
        dto.setCreatedAt(review.getCreatedAt());

        customerRepository.findById(review.getCustomerId()).ifPresent(c -> dto.setCustomerName(c.getCustomerName()));
        if (review.getVehicleId() != null) {
            vehicleRepository.findById(review.getVehicleId()).ifPresent(v -> {
                dto.setVehicleModel(v.getVehicleModel());
                dto.setRegistrationNumber(v.getRegistrationNumber());
            });
        }
        if (review.getJobCardId() != null) {
            jobCardRepository.findById(review.getJobCardId()).ifPresent(jc -> dto.setJobCardNumber(jc.getJobCardNumber()));
        }
        if (review.getInvoiceId() != null) {
            invoiceRepository.findById(review.getInvoiceId()).ifPresent(inv -> dto.setInvoiceNumber(inv.getInvoiceNumber()));
        }

        return dto;
    }
}
