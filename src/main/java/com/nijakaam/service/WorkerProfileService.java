package com.nijakaam.service;

import com.nijakaam.dto.request.WorkerProfileRequest;
import com.nijakaam.entity.WorkerProfile;

public interface WorkerProfileService {

    WorkerProfile createProfile(WorkerProfileRequest request);

    WorkerProfile createProfile(WorkerProfileRequest request, String userId);

    WorkerProfile getProfileByUserId(String userId);

    WorkerProfile getMyProfile();

    WorkerProfile updateProfile(String userId, WorkerProfileRequest request);

    void deleteProfile(String userId);

    WorkerProfile createProfile(WorkerProfile profile, String userId);

    WorkerProfile updateProfile(String userId, WorkerProfile profile);
}
