package com.nijakaam.service.impl;

import com.nijakaam.entity.WorkerProfile;
import com.nijakaam.exception.ResourceNotFoundException;
import com.nijakaam.repository.WorkerProfileRepository;
import com.nijakaam.service.WorkerProfileService;
import org.springframework.stereotype.Service;

@Service
public class WorkerProfileServiceImpl implements WorkerProfileService {

    private final WorkerProfileRepository workerProfileRepository;

    public WorkerProfileServiceImpl(WorkerProfileRepository workerProfileRepository) {
        this.workerProfileRepository = workerProfileRepository;
    }

    @Override
    public WorkerProfile createProfile(WorkerProfile profile, String userId) {

        if (workerProfileRepository.existsByUserId(userId)) {
            throw new RuntimeException("Worker profile already exists for this user");
        }

        profile.setUserId(userId);

        return workerProfileRepository.save(profile);
    }

    @Override
    public WorkerProfile getProfileByUserId(String userId) {

        return workerProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Worker profile not found"));
    }

    @Override
    public WorkerProfile updateProfile(String userId, WorkerProfile profile) {

        WorkerProfile existingProfile = getProfileByUserId(userId);

        existingProfile.setSkills(profile.getSkills());
        existingProfile.setExperienceYears(profile.getExperienceYears());
        existingProfile.setHourlyRate(profile.getHourlyRate());
        existingProfile.setBio(profile.getBio());
        existingProfile.setLocation(profile.getLocation());
        existingProfile.setAvailable(profile.isAvailable());

        return workerProfileRepository.save(existingProfile);
    }

    @Override
    public void deleteProfile(String userId) {

        WorkerProfile existingProfile = getProfileByUserId(userId);

        workerProfileRepository.delete(existingProfile);
    }
}