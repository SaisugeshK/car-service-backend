package com.example.InventoryManagementSystem.service;

import com.example.InventoryManagementSystem.Repository.UserRepository;
import com.example.InventoryManagementSystem.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

// Phase 30 — nothing in this backend previously resolved "who is making this request" past the
// JWT filter; every create/update method took a DTO with no notion of the authenticated caller.
// JwtAuthFilter sets the security principal to the user's email (see JwtAuthFilter/JwtUtil), so
// this just looks that email back up to a real User row — used wherever an action needs to be
// attributed to someone (the Audit Log), not invented per call site.
@Service
@RequiredArgsConstructor
public class CurrentUserService {

    private final UserRepository userRepository;

    public User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null || "anonymousUser".equals(auth.getName())) {
            return null;
        }
        Object found = userRepository.findByEmail(auth.getName()).orElse(null);
        return found instanceof User ? (User) found : null;
    }

    public Long getCurrentUserId() {
        User user = getCurrentUser();
        return user != null ? user.getUserId() : null;
    }

    public String getCurrentUsername() {
        User user = getCurrentUser();
        if (user == null) return "System";
        if (user.getFullName() != null && !user.getFullName().isBlank()) return user.getFullName();
        return user.getUsername() != null ? user.getUsername() : "Unknown";
    }
}
