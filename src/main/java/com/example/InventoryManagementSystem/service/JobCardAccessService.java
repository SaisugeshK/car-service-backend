package com.example.InventoryManagementSystem.service;

import com.example.InventoryManagementSystem.Repository.JobCardRepository;
import com.example.InventoryManagementSystem.exception.AccessDeniedException;
import com.example.InventoryManagementSystem.exception.ResourceNotFoundException;
import com.example.InventoryManagementSystem.model.JobCard;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Objects;

// An EMPLOYEE only ever sees job cards they're assigned to (as technician or service advisor) —
// and everything hanging off one (status history, inspection items/photos). A SUPER_ADMIN sees
// all. One place for the rule so every entry point applies it the same way.
@Service
@RequiredArgsConstructor
public class JobCardAccessService {

    private final JobCardRepository jobCardRepository;
    private final CurrentUserService currentUserService;

    public boolean canAccess(JobCard jobCard) {
        if (currentUserService.isSuperAdmin()) return true;
        Long me = currentUserService.getCurrentUserId();
        return me != null && (Objects.equals(me, jobCard.getTechnicianUserId())
                || Objects.equals(me, jobCard.getAdvisorUserId()));
    }

    public JobCard requireAccess(Long jobCardId) {
        JobCard jobCard = jobCardRepository.findById(jobCardId)
                .orElseThrow(() -> new ResourceNotFoundException("Job card not found with id: " + jobCardId));
        if (!canAccess(jobCard)) {
            throw new AccessDeniedException("This job card is not assigned to you");
        }
        return jobCard;
    }
}
