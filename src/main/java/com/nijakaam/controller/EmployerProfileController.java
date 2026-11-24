package com.nijakaam.controller;

import com.nijakaam.entity.EmployerProfile;
import com.nijakaam.service.EmployerProfileService;
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

    // Create employer profile
    @PostMapping("/{userId}")
    public ResponseEntity<EmployerProfile> createProfile(
            @PathVariable String userId,
            @RequestBody EmployerProfile profile) {

        EmployerProfile createdProfile = employerProfileService.createProfile(profile, userId);

        return new ResponseEntity<>(createdProfile, HttpStatus.CREATED);
    }

    // Get employer profile
    @GetMapping("/{userId}")
    public ResponseEntity<EmployerProfile> getProfile(
            @PathVariable String userId) {

        EmployerProfile profile = employerProfileService.getProfileByUserId(userId);

        return ResponseEntity.ok(profile);
    }

    // Update employer profile
    @PutMapping("/{userId}")
    public ResponseEntity<EmployerProfile> updateProfile(
            @PathVariable String userId,
            @RequestBody EmployerProfile profile) {

        EmployerProfile updatedProfile = employerProfileService.updateProfile(userId, profile);

        return ResponseEntity.ok(updatedProfile);
    }

    // Delete employer profile
    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteProfile(
            @PathVariable String userId) {

        employerProfileService.deleteProfile(userId);

        return ResponseEntity.noContent().build();
    }
}