package com.nijakaam.service;

import com.nijakaam.dto.request.EmployerProfileRequest;
import com.nijakaam.entity.EmployerProfile;

public interface EmployerProfileService {

    EmployerProfile createProfile(EmployerProfileRequest request);

    EmployerProfile createProfile(EmployerProfileRequest request, String userId);

    EmployerProfile getMyProfile();

    EmployerProfile getProfileByUserId(String userId);

    EmployerProfile updateProfile(String userId, EmployerProfileRequest request);

    void deleteProfile(String userId);

    // Overloads for backward compatibility
    EmployerProfile createProfile(EmployerProfile profile, String userId);

    EmployerProfile updateProfile(String userId, EmployerProfile profile);
}
