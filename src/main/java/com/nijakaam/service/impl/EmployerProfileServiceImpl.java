package com.nijakaam.service.impl;

import com.nijakaam.entity.EmployerProfile;
import com.nijakaam.exception.ResourceNotFoundException;
import com.nijakaam.repository.EmployerProfileRepository;
import com.nijakaam.service.EmployerProfileService;
import org.springframework.stereotype.Service;

@Service
public class EmployerProfileServiceImpl implements EmployerProfileService {

    private final EmployerProfileRepository employerProfileRepository;

    public EmployerProfileServiceImpl(EmployerProfileRepository employerProfileRepository) {
        this.employerProfileRepository = employerProfileRepository;
    }

    @Override
    public EmployerProfile createProfile(EmployerProfile profile, String userId) {

        if (employerProfileRepository.existsByUserId(userId)) {
            throw new RuntimeException("Employer profile already exists for this user");
        }

        profile.setUserId(userId);

        return employerProfileRepository.save(profile);
    }

    @Override
    public EmployerProfile getProfileByUserId(String userId) {

        return employerProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Employer profile not found"));
    }

    @Override
    public EmployerProfile updateProfile(String userId, EmployerProfile profile) {

        EmployerProfile existingProfile = getProfileByUserId(userId);

        existingProfile.setCompanyName(profile.getCompanyName());
        existingProfile.setCompanyDescription(profile.getCompanyDescription());
        existingProfile.setCompanyWebsite(profile.getCompanyWebsite());
        existingProfile.setLocation(profile.getLocation());
        existingProfile.setIndustry(profile.getIndustry());

        return employerProfileRepository.save(existingProfile);
    }

    @Override
    public void deleteProfile(String userId) {

        EmployerProfile existingProfile = getProfileByUserId(userId);

        employerProfileRepository.delete(existingProfile);
    }
}