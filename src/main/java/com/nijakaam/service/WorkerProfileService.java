package com.nijakaam.service;

import com.nijakaam.entity.WorkerProfile;

public interface WorkerProfileService {

    WorkerProfile createProfile(WorkerProfile profile, String userId);

    WorkerProfile getProfileByUserId(String userId);

    WorkerProfile updateProfile(String userId, WorkerProfile profile);

    void deleteProfile(String userId);
}