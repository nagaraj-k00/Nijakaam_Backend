package com.nijakaam.controller;

import com.nijakaam.dto.request.WorkerProfileRequest;
import com.nijakaam.dto.response.ApiResponse;
import com.nijakaam.dto.response.WorkerProfileResponse;
import com.nijakaam.entity.WorkerProfile;
import com.nijakaam.service.WorkerProfileService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/worker-profiles")
public class WorkerProfileController {

    private final WorkerProfileService workerProfileService;

    public WorkerProfileController(WorkerProfileService workerProfileService) {
        this.workerProfileService = workerProfileService;
    }

    // Create worker profile (canonical endpoint deriving userId from authenticated JWT)
    @PostMapping
    public ResponseEntity<ApiResponse<WorkerProfileResponse>> createProfile(
            @Valid @RequestBody WorkerProfileRequest request) {

        WorkerProfile createdProfile = workerProfileService.createProfile(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Worker profile created successfully", WorkerProfileResponse.fromEntity(createdProfile)));
    }

    // Get worker profile by userId
    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<WorkerProfileResponse>> getProfile(
            @PathVariable String userId) {

        WorkerProfile profile = workerProfileService.getProfileByUserId(userId);

        return ResponseEntity.ok(ApiResponse.success("Worker profile retrieved successfully", WorkerProfileResponse.fromEntity(profile)));
    }

    // Get current authenticated worker's profile
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<WorkerProfileResponse>> getMyProfile() {

        WorkerProfile profile = workerProfileService.getMyProfile();

        return ResponseEntity.ok(ApiResponse.success("Worker profile retrieved successfully", WorkerProfileResponse.fromEntity(profile)));
    }

    // Update worker profile
    @PutMapping("/{userId}")
    public ResponseEntity<ApiResponse<WorkerProfileResponse>> updateProfile(
            @PathVariable String userId,
            @Valid @RequestBody WorkerProfileRequest request) {

        WorkerProfile updatedProfile = workerProfileService.updateProfile(userId, request);

        return ResponseEntity.ok(ApiResponse.success("Worker profile updated successfully", WorkerProfileResponse.fromEntity(updatedProfile)));
    }

    // Delete worker profile
    @DeleteMapping("/{userId}")
    public ResponseEntity<ApiResponse<Void>> deleteProfile(
            @PathVariable String userId) {

        workerProfileService.deleteProfile(userId);

        return ResponseEntity.ok(ApiResponse.success("Worker profile deleted successfully", null));
    }
}
