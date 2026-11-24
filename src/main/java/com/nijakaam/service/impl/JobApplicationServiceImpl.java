package com.nijakaam.service.impl;

import com.nijakaam.entity.Job;
import com.nijakaam.entity.JobApplication;
import com.nijakaam.entity.User;
import com.nijakaam.enums.ApplicationStatus;
import com.nijakaam.enums.JobStatus;
import com.nijakaam.enums.Role;
import com.nijakaam.exception.ResourceNotFoundException;
import com.nijakaam.repository.JobApplicationRepository;
import com.nijakaam.repository.JobRepository;
import com.nijakaam.repository.UserRepository;
import com.nijakaam.service.JobApplicationService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class JobApplicationServiceImpl implements JobApplicationService {

    private final JobApplicationRepository jobApplicationRepository;
    private final JobRepository jobRepository;
    private final UserRepository userRepository;

    public JobApplicationServiceImpl(
            JobApplicationRepository jobApplicationRepository,
            JobRepository jobRepository,
            UserRepository userRepository) {

        this.jobApplicationRepository = jobApplicationRepository;
        this.jobRepository = jobRepository;
        this.userRepository = userRepository;
    }

    // Worker applies for a job
    @Override
    public JobApplication applyForJob(JobApplication application) {

        // Verify worker exists
        User worker = userRepository.findById(application.getWorkerId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Worker not found"));

        // Verify worker role
        if (worker.getRole() != Role.WORKER) {
            throw new RuntimeException("Only workers can apply for jobs");
        }

        // Verify job exists
        Job job = jobRepository.findById(application.getJobId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Job not found"));

        // Check job availability
        if (job.getStatus() != JobStatus.OPEN) {
            throw new RuntimeException("This job is not accepting applications");
        }

        // Prevent duplicate applications
        if (jobApplicationRepository.existsByWorkerIdAndJobId(
                application.getWorkerId(), application.getJobId())) {

            throw new RuntimeException("You have already applied for this job");
        }

        // Verify employer exists
        User employer = userRepository.findById(job.getEmployerId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Employer not found"));

        if (employer.getRole() != Role.EMPLOYER) {
            throw new RuntimeException("Invalid employer account");
        }

        // Set trusted employer ID from the job
        application.setEmployerId(job.getEmployerId());

        application.setStatus(ApplicationStatus.PENDING);
        application.setCreatedAt(LocalDateTime.now());
        application.setUpdatedAt(LocalDateTime.now());

        return jobApplicationRepository.save(application);
    }

    // Get application by ID
    @Override
    public JobApplication getApplicationById(String id) {

        return jobApplicationRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Application not found with id: " + id));
    }

    // Get worker applications
    @Override
    public List<JobApplication> getApplicationsByWorker(String workerId) {

        return jobApplicationRepository.findByWorkerId(workerId);
    }

    // Get employer applications
    @Override
    public List<JobApplication> getApplicationsByEmployer(String employerId) {

        return jobApplicationRepository.findByEmployerId(employerId);
    }

    // Get applications for a particular job
    @Override
    public List<JobApplication> getApplicationsByJob(String jobId) {

        return jobApplicationRepository.findByJobId(jobId);
    }

    // Accept or reject application
    @Override
    public JobApplication updateApplicationStatus(
            String id, ApplicationStatus status) {

        JobApplication application = getApplicationById(id);

        if (application.getStatus() != ApplicationStatus.PENDING) {
            throw new RuntimeException(
                    "Only pending applications can be updated");
        }

        if (status != ApplicationStatus.ACCEPTED
                && status != ApplicationStatus.REJECTED) {
            throw new RuntimeException("Invalid application status");
        }

        application.setStatus(status);
        application.setUpdatedAt(LocalDateTime.now());

        return jobApplicationRepository.save(application);
    }

    // Withdraw application
    @Override
    public void withdrawApplication(String id) {

        JobApplication application = getApplicationById(id);

        if (application.getStatus() != ApplicationStatus.PENDING) {
            throw new RuntimeException(
                    "Only pending applications can be withdrawn");
        }

        application.setStatus(ApplicationStatus.WITHDRAWN);
        application.setUpdatedAt(LocalDateTime.now());

        jobApplicationRepository.save(application);
    }
}
