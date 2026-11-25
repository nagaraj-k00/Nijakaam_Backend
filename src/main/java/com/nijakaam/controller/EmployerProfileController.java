package com.nijakaam.controller;

import com.nijakaam.dto.request.EmployerProfileRequest;
import com.nijakaam.dto.response.ApiResponse;
import com.nijakaam.dto.response.EmployerProfileResponse;
import com.nijakaam.entity.EmployerProfile;
import com.nijakaam.service.EmployerProfileService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/employer-profiles")
public class EmployerProfileController {

    private final EmployerProfileService employerProfileService;

    public EmployerProfileController(EmployerProfileService employerProfileService) {
        this.employerProfileService = employerProfileService;
    }

    // Create employer profile (canonical endpoint deriving userId from authenticated JWT)
    @PostMapping
    public ResponseEntity<ApiResponse<EmployerProfileResponse>> createProfile(
            @Valid @RequestBody EmployerProfileRequest request) {

        EmployerProfile createdProfile = employerProfileService.createProfile(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Employer profile created successfully", EmployerProfileResponse.fromEntity(createdProfile)));
    }

    // Create employer profile with path-based userId (backward compatibility, strictly checks authorization)
    @PostMapping("/{userId}")
    public ResponseEntity<ApiResponse<EmployerProfileResponse>> createProfileWithPath(
            @PathVariable String userId,
            @Valid @RequestBody EmployerProfileRequest request) {

        EmployerProfile createdProfile = employerProfileService.createProfile(request, userId);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Employer profile created successfully", EmployerProfileResponse.fromEntity(createdProfile)));
    }

    // Get current authenticated employer's profile
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<EmployerProfileResponse>> getMyProfile() {

        EmployerProfile profile = employerProfileService.getMyProfile();

        return ResponseEntity.ok(ApiResponse.success("Employer profile retrieved successfully", EmployerProfileResponse.fromEntity(profile)));
    }

    // Get employer profile by userId
    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<EmployerProfileResponse>> getProfile(
            @PathVariable String userId) {

        EmployerProfile profile = employerProfileService.getProfileByUserId(userId);

        return ResponseEntity.ok(ApiResponse.success("Employer profile retrieved successfully", EmployerProfileResponse.fromEntity(profile)));
    }

    // Update current authenticated employer's profile
    @PutMapping
    public ResponseEntity<ApiResponse<EmployerProfileResponse>> updateMyProfile(
            @Valid @RequestBody EmployerProfileRequest request) {

        EmployerProfile updatedProfile = employerProfileService.updateProfile(null, request);

        return ResponseEntity.ok(ApiResponse.success("Employer profile updated successfully", EmployerProfileResponse.fromEntity(updatedProfile)));
    }

    // Update employer profile
    @PutMapping("/{userId}")
    public ResponseEntity<ApiResponse<EmployerProfileResponse>> updateProfile(
            @PathVariable String userId,
            @Valid @RequestBody EmployerProfileRequest request) {

        EmployerProfile updatedProfile = employerProfileService.updateProfile(userId, request);

        return ResponseEntity.ok(ApiResponse.success("Employer profile updated successfully", EmployerProfileResponse.fromEntity(updatedProfile)));
    }

    // Delete employer profile
    @DeleteMapping("/{userId}")
    public ResponseEntity<ApiResponse<Void>> deleteProfile(
            @PathVariable String userId) {

        employerProfileService.deleteProfile(userId);

        return ResponseEntity.ok(ApiResponse.success("Employer profile deleted successfully", null));
    }
}
