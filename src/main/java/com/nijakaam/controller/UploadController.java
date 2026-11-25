package com.nijakaam.controller;

import com.nijakaam.dto.response.ApiResponse;
import com.nijakaam.entity.EmployerProfile;
import com.nijakaam.entity.User;
import com.nijakaam.entity.WorkerProfile;
import com.nijakaam.enums.Role;
import com.nijakaam.exception.ResourceNotFoundException;
import com.nijakaam.repository.EmployerProfileRepository;
import com.nijakaam.repository.UserRepository;
import com.nijakaam.repository.WorkerProfileRepository;
import com.nijakaam.service.FileUploadService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/uploads")
public class UploadController {

    private final FileUploadService fileUploadService;
    private final UserRepository userRepository;
    private final WorkerProfileRepository workerProfileRepository;
    private final EmployerProfileRepository employerProfileRepository;

    public UploadController(
            FileUploadService fileUploadService,
            UserRepository userRepository,
            WorkerProfileRepository workerProfileRepository,
            EmployerProfileRepository employerProfileRepository) {
        this.fileUploadService = fileUploadService;
        this.userRepository = userRepository;
        this.workerProfileRepository = workerProfileRepository;
        this.employerProfileRepository = employerProfileRepository;
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new AccessDeniedException("User is not authenticated");
        }
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }

    @PostMapping("/profile-photo")
    public ResponseEntity<ApiResponse<Map<String, String>>> uploadProfilePhoto(
            @RequestParam("file") MultipartFile file) {

        User currentUser = getCurrentUser();
        String imageUrl = fileUploadService.uploadProfilePhoto(file, currentUser.getId());

        // Update database according to user role
        if (currentUser.getRole() == Role.WORKER) {
            WorkerProfile profile = workerProfileRepository.findByUserId(currentUser.getId())
                    .orElseGet(() -> {
                        WorkerProfile newProfile = new WorkerProfile();
                        newProfile.setUserId(currentUser.getId());
                        newProfile.setLocation("Not specified");
                        newProfile.setCreatedAt(LocalDateTime.now());
                        return newProfile;
                    });
            profile.setAvatarUrl(imageUrl);
            profile.setUpdatedAt(LocalDateTime.now());
            workerProfileRepository.save(profile);

        } else if (currentUser.getRole() == Role.EMPLOYER) {
            EmployerProfile profile = employerProfileRepository.findByUserId(currentUser.getId())
                    .orElseGet(() -> {
                        EmployerProfile newProfile = new EmployerProfile();
                        newProfile.setUserId(currentUser.getId());
                        newProfile.setCompanyName(currentUser.getName() != null ? currentUser.getName() : "Company");
                        newProfile.setLocation("Not specified");
                        newProfile.setCreatedAt(LocalDateTime.now());
                        return newProfile;
                    });
            profile.setLogoUrl(imageUrl);
            profile.setUpdatedAt(LocalDateTime.now());
            employerProfileRepository.save(profile);
        }

        return ResponseEntity.ok(ApiResponse.success(
                "Profile photo uploaded and persisted successfully",
                Map.of(
                        "imageUrl", imageUrl,
                        "userId", currentUser.getId(),
                        "role", currentUser.getRole().name()
                )
        ));
    }
}
