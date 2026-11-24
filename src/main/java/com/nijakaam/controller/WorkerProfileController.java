package com.nijakaam.controller;

import com.nijakaam.entity.WorkerProfile;
import com.nijakaam.service.WorkerProfileService;
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

    // Create worker profile
    @PostMapping("/{userId}")
    public ResponseEntity<WorkerProfile> createProfile(
            @PathVariable String userId,
            @RequestBody WorkerProfile profile) {

        WorkerProfile createdProfile = workerProfileService.createProfile(profile, userId);

        return new ResponseEntity<>(createdProfile, HttpStatus.CREATED);
    }

    // Get worker profile
    @GetMapping("/{userId}")
    public ResponseEntity<WorkerProfile> getProfile(
            @PathVariable String userId) {

        WorkerProfile profile = workerProfileService.getProfileByUserId(userId);

        return ResponseEntity.ok(profile);
    }

    // Update worker profile
    @PutMapping("/{userId}")
    public ResponseEntity<WorkerProfile> updateProfile(
            @PathVariable String userId,
            @RequestBody WorkerProfile profile) {

        WorkerProfile updatedProfile = workerProfileService.updateProfile(userId, profile);

        return ResponseEntity.ok(updatedProfile);
    }

    // Delete worker profile
    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteProfile(
            @PathVariable String userId) {

        workerProfileService.deleteProfile(userId);

        return ResponseEntity.noContent().build();
    }
}