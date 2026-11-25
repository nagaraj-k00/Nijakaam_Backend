package com.nijakaam.service.impl;

import com.nijakaam.dto.request.WorkerProfileRequest;
import com.nijakaam.entity.User;
import com.nijakaam.entity.WorkerProfile;
import com.nijakaam.enums.Role;
import com.nijakaam.exception.ResourceNotFoundException;
import com.nijakaam.repository.UserRepository;
import com.nijakaam.repository.WorkerProfileRepository;
import com.nijakaam.service.WorkerProfileService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;

@Service
public class WorkerProfileServiceImpl implements WorkerProfileService {

    private final WorkerProfileRepository workerProfileRepository;
    private final UserRepository userRepository;

    public WorkerProfileServiceImpl(
            WorkerProfileRepository workerProfileRepository,
            UserRepository userRepository) {
        this.workerProfileRepository = workerProfileRepository;
        this.userRepository = userRepository;
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null ||
                !authentication.isAuthenticated() ||
                "anonymousUser".equals(authentication.getPrincipal())) {
            throw new AccessDeniedException("User is not authenticated");
        }

        String email = authentication.getName();
        if (email == null || email.isBlank()) {
            throw new AccessDeniedException("Authenticated user identity is missing");
        }

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    @Override
    public WorkerProfile createProfile(WorkerProfileRequest request) {
        return createProfile(request, null);
    }

    @Override
    public WorkerProfile createProfile(WorkerProfileRequest request, String userId) {
        User currentUser = getCurrentUser();

        if (currentUser.getRole() != Role.WORKER && currentUser.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("Only workers can create a worker profile");
        }

        // If client passes another user's ID in path, ensure worker cannot create profile for someone else
        if (currentUser.getRole() != Role.ADMIN && userId != null && !userId.isBlank() && !userId.equals(currentUser.getId())) {
            throw new AccessDeniedException("You are not authorized to create a profile for another user");
        }

        // Never trust client-supplied workerId/userId from request body or path; derive directly from authenticated worker
        String targetUserId = (currentUser.getRole() == Role.ADMIN && userId != null && !userId.isBlank())
                ? userId
                : currentUser.getId();

        if (workerProfileRepository.existsByUserId(targetUserId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Worker profile already exists for this user");
        }

        WorkerProfile profile = new WorkerProfile();
        profile.setUserId(targetUserId);
        profile.setSkills(request.getSkills() != null ? new ArrayList<>(request.getSkills()) : new ArrayList<>());
        profile.setExperienceYears(request.getExperienceYears());
        profile.setHourlyRate(request.getHourlyRate());
        profile.setDailyRate(request.getDailyRate());
        if (request.getCertifications() != null) {
            profile.setCertifications(new ArrayList<>(request.getCertifications()));
        }
        profile.setPreferredShift(request.getPreferredShift());
        profile.setAvatarUrl(request.getAvatarUrl());
        profile.setBio(request.getBio());
        profile.setLocation(request.getLocation());
        profile.setAvailable(request.getAvailable() != null ? request.getAvailable() : true);
        profile.setRating(0.0);
        profile.setCreatedAt(LocalDateTime.now());
        profile.setUpdatedAt(LocalDateTime.now());

        return workerProfileRepository.save(profile);
    }

    @Override
    public WorkerProfile getProfileByUserId(String userId) {
        User currentUser = getCurrentUser();

        String targetUserId = (userId != null && !userId.isBlank()) ? userId : currentUser.getId();

        // Worker can retrieve only their own private profile; Admin can access; Employers cannot access private worker profiles
        if (currentUser.getRole() != Role.ADMIN && !currentUser.getId().equals(targetUserId)) {
            throw new AccessDeniedException("You are not authorized to view this profile");
        }

        return workerProfileRepository.findByUserId(targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Worker profile not found for user id: " + targetUserId));
    }

    @Override
    public WorkerProfile getMyProfile() {
        User currentUser = getCurrentUser();

        if (currentUser.getRole() != Role.WORKER && currentUser.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("Only workers can access their worker profile");
        }

        return workerProfileRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Worker profile not found for user id: " + currentUser.getId()));
    }

    @Override
    public WorkerProfile updateProfile(String userId, WorkerProfileRequest request) {
        User currentUser = getCurrentUser();

        if (currentUser.getRole() == Role.EMPLOYER) {
            throw new AccessDeniedException("Only workers can modify worker profiles");
        }

        String targetUserId = (userId != null && !userId.isBlank()) ? userId : currentUser.getId();

        if (currentUser.getRole() != Role.ADMIN && !currentUser.getId().equals(targetUserId)) {
            throw new AccessDeniedException("You are not authorized to modify this profile");
        }

        WorkerProfile existingProfile = workerProfileRepository.findByUserId(targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Worker profile not found for user id: " + targetUserId));

        if (currentUser.getRole() != Role.ADMIN && !existingProfile.getUserId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You are not authorized to modify this profile");
        }

        // Update only allowed fields; preserve id, userId, rating, createdAt
        if (request.getSkills() != null) {
            existingProfile.setSkills(new ArrayList<>(request.getSkills()));
        }
        if (request.getExperienceYears() != null) {
            existingProfile.setExperienceYears(request.getExperienceYears());
        }
        if (request.getHourlyRate() != null) {
            existingProfile.setHourlyRate(request.getHourlyRate());
        }
        if (request.getDailyRate() != null) {
            existingProfile.setDailyRate(request.getDailyRate());
        }
        if (request.getCertifications() != null) {
            existingProfile.setCertifications(new ArrayList<>(request.getCertifications()));
        }
        if (request.getPreferredShift() != null) {
            existingProfile.setPreferredShift(request.getPreferredShift());
        }
        if (request.getAvatarUrl() != null) {
            existingProfile.setAvatarUrl(request.getAvatarUrl());
        }
        if (request.getBio() != null) {
            existingProfile.setBio(request.getBio());
        }
        if (request.getLocation() != null) {
            existingProfile.setLocation(request.getLocation());
        }
        if (request.getAvailable() != null) {
            existingProfile.setAvailable(request.getAvailable());
        }
        existingProfile.setUpdatedAt(LocalDateTime.now());

        return workerProfileRepository.save(existingProfile);
    }

    @Override
    public void deleteProfile(String userId) {
        User currentUser = getCurrentUser();

        if (currentUser.getRole() == Role.EMPLOYER) {
            throw new AccessDeniedException("Only workers can delete worker profiles");
        }

        String targetUserId = (userId != null && !userId.isBlank()) ? userId : currentUser.getId();

        if (currentUser.getRole() != Role.ADMIN && !currentUser.getId().equals(targetUserId)) {
            throw new AccessDeniedException("You are not authorized to delete this profile");
        }

        WorkerProfile existingProfile = workerProfileRepository.findByUserId(targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Worker profile not found for user id: " + targetUserId));

        if (currentUser.getRole() != Role.ADMIN && !existingProfile.getUserId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You are not authorized to delete this profile");
        }

        workerProfileRepository.delete(existingProfile);
    }

    @Override
    public WorkerProfile createProfile(WorkerProfile profile, String userId) {
        WorkerProfileRequest request = new WorkerProfileRequest(
                profile.getSkills(),
                profile.getExperienceYears(),
                profile.getHourlyRate(),
                profile.getBio(),
                profile.getLocation(),
                profile.isAvailable()
        );
        return createProfile(request, userId);
    }

    @Override
    public WorkerProfile updateProfile(String userId, WorkerProfile profile) {
        WorkerProfileRequest request = new WorkerProfileRequest(
                profile.getSkills(),
                profile.getExperienceYears(),
                profile.getHourlyRate(),
                profile.getBio(),
                profile.getLocation(),
                profile.isAvailable()
        );
        return updateProfile(userId, request);
    }
}
