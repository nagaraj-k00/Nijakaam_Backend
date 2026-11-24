package com.nijakaam.service.impl;

import com.nijakaam.dto.request.EmployerProfileRequest;
import com.nijakaam.entity.EmployerProfile;
import com.nijakaam.entity.User;
import com.nijakaam.enums.Role;
import com.nijakaam.exception.ResourceNotFoundException;
import com.nijakaam.repository.EmployerProfileRepository;
import com.nijakaam.repository.UserRepository;
import com.nijakaam.service.EmployerProfileService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Service
public class EmployerProfileServiceImpl implements EmployerProfileService {

    private final EmployerProfileRepository employerProfileRepository;
    private final UserRepository userRepository;

    public EmployerProfileServiceImpl(
            EmployerProfileRepository employerProfileRepository,
            UserRepository userRepository) {
        this.employerProfileRepository = employerProfileRepository;
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
    public EmployerProfile createProfile(EmployerProfileRequest request) {
        return createProfile(request, null);
    }

    @Override
    public EmployerProfile createProfile(EmployerProfileRequest request, String userId) {
        User currentUser = getCurrentUser();

        if (currentUser.getRole() != Role.EMPLOYER && currentUser.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("Only employers can create an employer profile");
        }

        // If client passes another user's ID in path, ensure employer cannot create profile for someone else
        if (currentUser.getRole() != Role.ADMIN && userId != null && !userId.isBlank() && !userId.equals(currentUser.getId())) {
            throw new AccessDeniedException("You are not authorized to create a profile for another user");
        }

        // Never trust client-supplied employerId/userId from request body or path; derive directly from authenticated user
        String targetUserId = (currentUser.getRole() == Role.ADMIN && userId != null && !userId.isBlank())
                ? userId
                : currentUser.getId();

        if (employerProfileRepository.existsByUserId(targetUserId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Employer profile already exists for this user");
        }

        EmployerProfile profile = new EmployerProfile();
        profile.setUserId(targetUserId);
        profile.setCompanyName(request.getCompanyName());
        profile.setCompanyDescription(request.getCompanyDescription());
        profile.setCompanyWebsite(request.getCompanyWebsite());
        profile.setLocation(request.getLocation());
        profile.setIndustry(request.getIndustry());
        profile.setCreatedAt(LocalDateTime.now());
        profile.setUpdatedAt(LocalDateTime.now());

        return employerProfileRepository.save(profile);
    }

    @Override
    public EmployerProfile getMyProfile() {
        User currentUser = getCurrentUser();

        if (currentUser.getRole() != Role.EMPLOYER && currentUser.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("Only employers can access their employer profile");
        }

        return employerProfileRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Employer profile not found for user id: " + currentUser.getId()));
    }

    @Override
    public EmployerProfile getProfileByUserId(String userId) {
        User currentUser = getCurrentUser();

        String targetUserId = (userId != null && !userId.isBlank()) ? userId : currentUser.getId();

        // Employer can retrieve only their own private profile; Admin can access; Workers cannot access private employer profiles
        if (currentUser.getRole() != Role.ADMIN && !currentUser.getId().equals(targetUserId)) {
            throw new AccessDeniedException("You are not authorized to view this profile");
        }

        return employerProfileRepository.findByUserId(targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Employer profile not found for user id: " + targetUserId));
    }

    @Override
    public EmployerProfile updateProfile(String userId, EmployerProfileRequest request) {
        User currentUser = getCurrentUser();

        if (currentUser.getRole() == Role.WORKER) {
            throw new AccessDeniedException("Only employers can modify employer profiles");
        }

        String targetUserId = (userId != null && !userId.isBlank()) ? userId : currentUser.getId();

        if (currentUser.getRole() != Role.ADMIN && !currentUser.getId().equals(targetUserId)) {
            throw new AccessDeniedException("You are not authorized to modify this profile");
        }

        EmployerProfile existingProfile = employerProfileRepository.findByUserId(targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Employer profile not found for user id: " + targetUserId));

        if (currentUser.getRole() != Role.ADMIN && !existingProfile.getUserId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You are not authorized to modify this profile");
        }

        // Update only allowed fields; preserve id, userId, createdAt
        if (request.getCompanyName() != null) {
            existingProfile.setCompanyName(request.getCompanyName());
        }
        if (request.getCompanyDescription() != null) {
            existingProfile.setCompanyDescription(request.getCompanyDescription());
        }
        if (request.getCompanyWebsite() != null) {
            existingProfile.setCompanyWebsite(request.getCompanyWebsite());
        }
        if (request.getLocation() != null) {
            existingProfile.setLocation(request.getLocation());
        }
        if (request.getIndustry() != null) {
            existingProfile.setIndustry(request.getIndustry());
        }
        existingProfile.setUpdatedAt(LocalDateTime.now());

        return employerProfileRepository.save(existingProfile);
    }

    @Override
    public void deleteProfile(String userId) {
        User currentUser = getCurrentUser();

        if (currentUser.getRole() == Role.WORKER) {
            throw new AccessDeniedException("Only employers can delete employer profiles");
        }

        String targetUserId = (userId != null && !userId.isBlank()) ? userId : currentUser.getId();

        if (currentUser.getRole() != Role.ADMIN && !currentUser.getId().equals(targetUserId)) {
            throw new AccessDeniedException("You are not authorized to delete this profile");
        }

        EmployerProfile existingProfile = employerProfileRepository.findByUserId(targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Employer profile not found for user id: " + targetUserId));

        if (currentUser.getRole() != Role.ADMIN && !existingProfile.getUserId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You are not authorized to delete this profile");
        }

        employerProfileRepository.delete(existingProfile);
    }

    @Override
    public EmployerProfile createProfile(EmployerProfile profile, String userId) {
        EmployerProfileRequest request = new EmployerProfileRequest(
                profile.getCompanyName(),
                profile.getCompanyDescription(),
                profile.getCompanyWebsite(),
                profile.getLocation(),
                profile.getIndustry()
        );
        return createProfile(request, userId);
    }

    @Override
    public EmployerProfile updateProfile(String userId, EmployerProfile profile) {
        EmployerProfileRequest request = new EmployerProfileRequest(
                profile.getCompanyName(),
                profile.getCompanyDescription(),
                profile.getCompanyWebsite(),
                profile.getLocation(),
                profile.getIndustry()
        );
        return updateProfile(userId, request);
    }
}
