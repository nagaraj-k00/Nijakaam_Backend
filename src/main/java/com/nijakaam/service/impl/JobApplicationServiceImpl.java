
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
import com.nijakaam.repository.WorkerProfileRepository;

import com.nijakaam.enums.NotificationType;
import com.nijakaam.service.JobApplicationService;
import com.nijakaam.service.NotificationService;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class JobApplicationServiceImpl implements JobApplicationService {

    private final JobApplicationRepository jobApplicationRepository;
    private final JobRepository jobRepository;
    private final UserRepository userRepository;
    private final WorkerProfileRepository workerProfileRepository;
    private final NotificationService notificationService;

    public JobApplicationServiceImpl(
            JobApplicationRepository jobApplicationRepository,
            JobRepository jobRepository,
            UserRepository userRepository,
            WorkerProfileRepository workerProfileRepository,
            NotificationService notificationService) {
        this.jobApplicationRepository = jobApplicationRepository;
        this.jobRepository = jobRepository;
        this.userRepository = userRepository;
        this.workerProfileRepository = workerProfileRepository;
        this.notificationService = notificationService;
    }

    // Get currently authenticated user from JWT
    private User getCurrentUser() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null ||
                !authentication.isAuthenticated() ||
                authentication.getPrincipal().equals("anonymousUser")) {

            throw new AccessDeniedException("User is not authenticated");
        }

        String email = authentication.getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    // Verify user role
    private void validateRole(User user, Role expectedRole) {

        if (user.getRole() != expectedRole) {
            throw new AccessDeniedException(
                    "Only " + expectedRole + " users can perform this operation");
        }
    }

    // Verify employer owns the job
    private Job getOwnedJob(String jobId, User employer) {

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));

        if (!job.getEmployerId().equals(employer.getId())) {
            throw new AccessDeniedException(
                    "You are not authorized to access this job");
        }

        return job;
    }

    // Verify access to an application
    private JobApplication getAccessibleApplication(String id, User user) {

        JobApplication application = jobApplicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found"));

        if (user.getRole() == Role.ADMIN) {
            return application;
        }

        boolean isWorker = user.getRole() == Role.WORKER &&
                application.getWorkerId().equals(user.getId());

        boolean isEmployer = user.getRole() == Role.EMPLOYER &&
                application.getEmployerId().equals(user.getId());

        if (!isWorker && !isEmployer) {
            throw new AccessDeniedException(
                    "You are not authorized to access this application");
        }

        return application;
    }

    // Worker applies for a job
    @Override
    public JobApplication applyForJob(JobApplication application) {

        User worker = getCurrentUser();

        validateRole(worker, Role.WORKER);

        Job job = jobRepository.findById(application.getJobId())
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));

        if (job.getStatus() != JobStatus.OPEN) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This job is not accepting applications");
        }

        if (jobApplicationRepository.existsByWorkerIdAndJobId(
                worker.getId(), job.getId())) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "You have already applied for this job");
        }

        User employer = userRepository.findById(job.getEmployerId())
                .orElseThrow(() -> new ResourceNotFoundException("Employer not found"));

        validateRole(employer, Role.EMPLOYER);

        // Set trusted IDs from database, not request body
        application.setWorkerId(worker.getId());
        application.setEmployerId(employer.getId());
        application.setWorkerName(worker.getName());
        application.setWorkerEmail(worker.getEmail());
        application.setWorkerPhone(worker.getPhone());
        application.setJobTitle(job.getTitle());

        workerProfileRepository.findByUserId(worker.getId()).ifPresent(profile -> {
            if (profile.getSkills() != null) {
                application.setWorkerSkills(profile.getSkills());
            }
            application.setWorkerLocation(profile.getLocation());
            application.setWorkerRating(profile.getRating());
            application.setExperienceYears(profile.getExperienceYears());
            application.setWorkerAvatarUrl(profile.getAvatarUrl());
        });

        application.setStatus(ApplicationStatus.PENDING);

        LocalDateTime now = LocalDateTime.now();

        application.setCreatedAt(now);
        application.setUpdatedAt(now);

        JobApplication saved = jobApplicationRepository.save(application);

        notificationService.sendNotification(
                employer.getId(),
                "New Job Application",
                "A worker has applied for your job: " + job.getTitle(),
                NotificationType.NEW_JOB_APPLICATION,
                saved.getId()
        );

        return saved;
    }

    private JobApplication enrichApplication(JobApplication app) {
        if (app == null) return null;
        if (app.getWorkerName() == null || app.getJobTitle() == null) {
            userRepository.findById(app.getWorkerId()).ifPresent(u -> {
                if (app.getWorkerName() == null) app.setWorkerName(u.getName());
                if (app.getWorkerEmail() == null) app.setWorkerEmail(u.getEmail());
                if (app.getWorkerPhone() == null) app.setWorkerPhone(u.getPhone());
            });
            if (app.getJobTitle() == null && app.getJobId() != null) {
                jobRepository.findById(app.getJobId()).ifPresent(j -> app.setJobTitle(j.getTitle()));
            }
            if (app.getWorkerSkills() == null || app.getWorkerSkills().isEmpty()) {
                workerProfileRepository.findByUserId(app.getWorkerId()).ifPresent(p -> {
                    if (p.getSkills() != null) app.setWorkerSkills(p.getSkills());
                    if (app.getWorkerLocation() == null) app.setWorkerLocation(p.getLocation());
                    if (app.getWorkerRating() == null) app.setWorkerRating(p.getRating());
                    if (app.getExperienceYears() == null) app.setExperienceYears(p.getExperienceYears());
                    if (app.getWorkerAvatarUrl() == null) app.setWorkerAvatarUrl(p.getAvatarUrl());
                });
            }
        }
        return app;
    }

    // Get application by ID
    @Override
    public JobApplication getApplicationById(String id) {

        User currentUser = getCurrentUser();

        return enrichApplication(getAccessibleApplication(id, currentUser));
    }

    // Get worker applications
    @Override
    public List<JobApplication> getApplicationsByWorker(String workerId) {

        User currentUser = getCurrentUser();

        if (currentUser.getRole() == Role.ADMIN) {
            return jobApplicationRepository.findByWorkerId(workerId).stream().map(this::enrichApplication).toList();
        }

        validateRole(currentUser, Role.WORKER);

        if (!currentUser.getId().equals(workerId)) {
            throw new AccessDeniedException(
                    "You can only view your own applications");
        }

        return jobApplicationRepository.findByWorkerId(currentUser.getId()).stream().map(this::enrichApplication).toList();
    }

    // Get employer applications
    @Override
    public List<JobApplication> getApplicationsByEmployer(String employerId) {

        User currentUser = getCurrentUser();

        if (currentUser.getRole() == Role.ADMIN) {
            return jobApplicationRepository.findByEmployerId(employerId).stream().map(this::enrichApplication).toList();
        }

        validateRole(currentUser, Role.EMPLOYER);

        if (!currentUser.getId().equals(employerId)) {
            throw new AccessDeniedException(
                    "You can only view your own applications");
        }

        return jobApplicationRepository.findByEmployerId(currentUser.getId()).stream().map(this::enrichApplication).toList();
    }

    // Get applications for a particular job
    @Override
    public List<JobApplication> getApplicationsByJob(String jobId) {

        User currentUser = getCurrentUser();

        if (currentUser.getRole() == Role.ADMIN) {
            return jobApplicationRepository.findByJobId(jobId).stream().map(this::enrichApplication).toList();
        }

        validateRole(currentUser, Role.EMPLOYER);

        getOwnedJob(jobId, currentUser);

        return jobApplicationRepository.findByJobId(jobId).stream().map(this::enrichApplication).toList();
    }

    // Employer accepts or rejects application
    @Override
    public JobApplication updateApplicationStatus(
            String id,
            ApplicationStatus status) {

        User employer = getCurrentUser();

        validateRole(employer, Role.EMPLOYER);

        JobApplication application = getAccessibleApplication(id, employer);

        if (!application.getEmployerId().equals(employer.getId())) {
            throw new AccessDeniedException(
                    "You cannot update this application");
        }

        if (application.getStatus() != ApplicationStatus.PENDING &&
                application.getStatus() != ApplicationStatus.SHORTLISTED) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Only pending or shortlisted applications can be updated");
        }

        if (status != ApplicationStatus.ACCEPTED &&
                status != ApplicationStatus.REJECTED &&
                status != ApplicationStatus.SHORTLISTED) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Only ACCEPTED, REJECTED, or SHORTLISTED status is allowed");
        }

        application.setStatus(status);
        application.setUpdatedAt(LocalDateTime.now());

        JobApplication saved = jobApplicationRepository.save(application);

        NotificationType notificationType;
        String title;
        String message;

        if (status == ApplicationStatus.ACCEPTED) {
            notificationType = NotificationType.APPLICATION_ACCEPTED;
            title = "Application Accepted";
            message = "Your job application has been accepted!";
        } else if (status == ApplicationStatus.SHORTLISTED) {
            notificationType = NotificationType.APPLICATION_SHORTLISTED;
            title = "Application Shortlisted";
            message = "Your job application has been shortlisted!";
        } else {
            notificationType = NotificationType.APPLICATION_REJECTED;
            title = "Application Rejected";
            message = "Your job application has been rejected.";
        }

        notificationService.sendNotification(
                saved.getWorkerId(),
                title,
                message,
                notificationType,
                saved.getId()
        );

        return enrichApplication(saved);
    }

    // Worker withdraws their own application
    @Override
    public void withdrawApplication(String id) {

        User worker = getCurrentUser();

        validateRole(worker, Role.WORKER);

        JobApplication application = getAccessibleApplication(id, worker);

        if (!application.getWorkerId().equals(worker.getId())) {
            throw new AccessDeniedException(
                    "You can only withdraw your own application");
        }

        if (application.getStatus() != ApplicationStatus.PENDING) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Only pending applications can be withdrawn");
        }

        application.setStatus(ApplicationStatus.WITHDRAWN);
        application.setUpdatedAt(LocalDateTime.now());

        jobApplicationRepository.save(application);

        notificationService.sendNotification(
                application.getEmployerId(),
                "Application Withdrawn",
                "A worker has withdrawn their job application.",
                NotificationType.APPLICATION_WITHDRAWN,
                application.getId()
        );
    }
}
