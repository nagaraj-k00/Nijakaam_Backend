package com.nijakaam.service;

import com.nijakaam.entity.JobApplication;
import com.nijakaam.enums.ApplicationStatus;

import java.util.List;

public interface JobApplicationService {

    // Worker applies for a job
    JobApplication applyForJob(JobApplication application);

    // Get application by ID
    JobApplication getApplicationById(String id);

    // Get all applications submitted by a worker
    List<JobApplication> getApplicationsByWorker(String workerId);

    // Get all applications received by an employer
    List<JobApplication> getApplicationsByEmployer(String employerId);

    // Get all applications for a particular job
    List<JobApplication> getApplicationsByJob(String jobId);

    // Accept or reject an application
    JobApplication updateApplicationStatus(String id, ApplicationStatus status);

    // Worker withdraws an application
    void withdrawApplication(String id);
}
