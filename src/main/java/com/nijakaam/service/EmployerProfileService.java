package com.nijakaam.service;

import com.nijakaam.entity.EmployerProfile;

public interface EmployerProfileService {

    EmployerProfile createProfile(EmployerProfile profile, String userId);

    EmployerProfile getProfileByUserId(String userId);

    EmployerProfile updateProfile(String userId, EmployerProfile profile);

    void deleteProfile(String userId);
}