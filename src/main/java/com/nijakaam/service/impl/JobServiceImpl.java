package com.nijakaam.service.impl;

import com.nijakaam.entity.Job;
import com.nijakaam.entity.User;
import com.nijakaam.enums.JobStatus;
import com.nijakaam.enums.Role;
import com.nijakaam.exception.ResourceNotFoundException;
import com.nijakaam.repository.JobRepository;
import com.nijakaam.repository.UserRepository;
import com.nijakaam.service.JobService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class JobServiceImpl implements JobService {

    private final JobRepository jobRepository;
    private final UserRepository userRepository;

    public JobServiceImpl(
            JobRepository jobRepository,
            UserRepository userRepository) {
        this.jobRepository = jobRepository;
        this.userRepository = userRepository;
    }

    private User getCurrentEmployer() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null ||
                !authentication.isAuthenticated() ||
                "anonymousUser".equals(authentication.getPrincipal())) {
            throw new AccessDeniedException("User is not authenticated");
        }

        String email = authentication.getName();
        if (email == null || email.isBlank()) {
            throw new AccessDeniedException("Authenticated user identity is missing");
        }

        User employer = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Employer not found"));

        if (employer.getRole() != Role.EMPLOYER) {
            throw new AccessDeniedException("Only employers can perform this operation");
        }

        return employer;
    }

    private Job getOwnedJob(String id, String employerId) {
        Job job = getJobById(id);

        if (job.getEmployerId() == null || !job.getEmployerId().equals(employerId)) {
            throw new AccessDeniedException("You are not authorized to modify this job");
        }

        return job;
    }

    @Override
    public Job createJob(Job job) {
        User employer = getCurrentEmployer();

        job.setEmployerId(employer.getId());
        job.setStatus(JobStatus.OPEN);
        job.setCreatedAt(LocalDateTime.now());
        job.setUpdatedAt(LocalDateTime.now());

        return jobRepository.save(job);
    }

    @Override
    public Job getJobById(String id) {
        return jobRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Job not found with id: " + id));
    }

    @Override
    public List<Job> getAllJobs() {
        return jobRepository.findAll();
    }

    @Override
    public List<Job> getJobsByEmployerId(String employerId) {
        return jobRepository.findByEmployerId(employerId);
    }

    @Override
    public List<Job> getJobsByStatus(JobStatus status) {
        return jobRepository.findByStatus(status);
    }

    @Override
    public Job updateJobStatus(String id, JobStatus status) {
        User employer = getCurrentEmployer();

        Job job = getOwnedJob(id, employer.getId());

        job.setStatus(status);
        job.setUpdatedAt(LocalDateTime.now());

        return jobRepository.save(job);
    }

    @Override
    public Job updateJob(String id, Job updatedJob) {
        User employer = getCurrentEmployer();

        Job existingJob = getOwnedJob(id, employer.getId());

        existingJob.setTitle(updatedJob.getTitle());
        existingJob.setDescription(updatedJob.getDescription());
        existingJob.setSkillsRequired(updatedJob.getSkillsRequired());
        existingJob.setBudget(updatedJob.getBudget());
        existingJob.setLocation(updatedJob.getLocation());
        existingJob.setUpdatedAt(LocalDateTime.now());

        return jobRepository.save(existingJob);
    }

    @Override
    public void deleteJob(String id) {
        User employer = getCurrentEmployer();

        Job job = getOwnedJob(id, employer.getId());

        jobRepository.delete(job);
    }
}
