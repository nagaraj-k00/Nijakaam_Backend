package com.nijakaam.service.impl;

import com.nijakaam.dto.request.JobSearchCriteria;
import com.nijakaam.dto.response.JobResponse;
import com.nijakaam.dto.response.PagedResponse;
import com.nijakaam.entity.Job;
import com.nijakaam.entity.User;
import com.nijakaam.enums.JobStatus;
import com.nijakaam.enums.Role;
import com.nijakaam.exception.ResourceNotFoundException;
import com.nijakaam.repository.JobRepository;
import com.nijakaam.repository.UserRepository;
import com.nijakaam.service.JobService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

@Service
public class JobServiceImpl implements JobService {

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "createdAt", "budget", "title", "updatedAt"
    );

    private final JobRepository jobRepository;
    private final UserRepository userRepository;
    private final MongoTemplate mongoTemplate;

    public JobServiceImpl(
            JobRepository jobRepository,
            UserRepository userRepository,
            MongoTemplate mongoTemplate) {
        this.jobRepository = jobRepository;
        this.userRepository = userRepository;
        this.mongoTemplate = mongoTemplate;
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

    private User getCurrentUser() {
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

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
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

        // Verify that the job belongs to the authenticated employer
        Job existingJob = getOwnedJob(id, employer.getId());

        // Update only permitted fields
        existingJob.setTitle(updatedJob.getTitle());
        existingJob.setDescription(updatedJob.getDescription());

        existingJob.setSkillsRequired(
                updatedJob.getSkillsRequired() != null
                        ? new java.util.ArrayList<>(updatedJob.getSkillsRequired())
                        : new java.util.ArrayList<>());

        existingJob.setBudget(updatedJob.getBudget());
        existingJob.setLocation(updatedJob.getLocation());

        if (updatedJob.getCategory() != null) {
            existingJob.setCategory(updatedJob.getCategory());
        }
        if (updatedJob.getOpenings() != null) {
            existingJob.setOpenings(updatedJob.getOpenings());
        }
        if (updatedJob.getEngagementModel() != null) {
            existingJob.setEngagementModel(updatedJob.getEngagementModel());
        }
        if (updatedJob.getExperienceLevel() != null) {
            existingJob.setExperienceLevel(updatedJob.getExperienceLevel());
        }
        if (updatedJob.getWageCadence() != null) {
            existingJob.setWageCadence(updatedJob.getWageCadence());
        }
        if (updatedJob.getShiftTiming() != null) {
            existingJob.setShiftTiming(updatedJob.getShiftTiming());
        }
        existingJob.setUrgent(updatedJob.isUrgent());

        // Preserve employerId, status, createdAt and job ID
        existingJob.setUpdatedAt(LocalDateTime.now());

        return jobRepository.save(existingJob);
    }

    @Override
    public void deleteJob(String id) {
        User employer = getCurrentEmployer();

        Job job = getOwnedJob(id, employer.getId());

        jobRepository.delete(job);
    }

    @Override
    public PagedResponse<JobResponse> searchJobs(JobSearchCriteria criteria) {
        User currentUser = getCurrentUser();

        // Business Rule: Workers can search and browse OPEN jobs; closed jobs are excluded from default worker search
        if (currentUser.getRole() == Role.WORKER) {
            criteria.setStatus(JobStatus.OPEN);
        }

        validateCriteria(criteria);

        Query query = new Query();

        // 1. Keyword search (case-insensitive across title or description)
        if (criteria.getKeyword() != null && !criteria.getKeyword().isBlank()) {
            String quoted = Pattern.quote(criteria.getKeyword().trim());
            query.addCriteria(new Criteria().orOperator(
                    Criteria.where("title").regex(quoted, "i"),
                    Criteria.where("description").regex(quoted, "i")
            ));
        }

        // 2. Skill filtering (case-insensitive matching on skillsRequired array)
        if (criteria.getSkill() != null && !criteria.getSkill().isBlank()) {
            query.addCriteria(Criteria.where("skillsRequired").regex(Pattern.quote(criteria.getSkill().trim()), "i"));
        }

        // 3. Location filtering (case-insensitive)
        if (criteria.getLocation() != null && !criteria.getLocation().isBlank()) {
            query.addCriteria(Criteria.where("location").regex(Pattern.quote(criteria.getLocation().trim()), "i"));
        }

        // 4. Budget range filtering
        if (criteria.getMinBudget() != null && criteria.getMaxBudget() != null) {
            query.addCriteria(Criteria.where("budget").gte(criteria.getMinBudget()).lte(criteria.getMaxBudget()));
        } else if (criteria.getMinBudget() != null) {
            query.addCriteria(Criteria.where("budget").gte(criteria.getMinBudget()));
        } else if (criteria.getMaxBudget() != null) {
            query.addCriteria(Criteria.where("budget").lte(criteria.getMaxBudget()));
        }

        // 5. Job status filtering
        if (criteria.getStatus() != null) {
            query.addCriteria(Criteria.where("status").is(criteria.getStatus()));
        }

        // 6. Category filtering
        if (criteria.getCategory() != null && !criteria.getCategory().isBlank()) {
            query.addCriteria(Criteria.where("category").regex(Pattern.quote(criteria.getCategory().trim()), "i"));
        }

        // 7. Engagement model filtering
        if (criteria.getEngagementModel() != null && !criteria.getEngagementModel().isBlank()) {
            query.addCriteria(Criteria.where("engagementModel").regex(Pattern.quote(criteria.getEngagementModel().trim()), "i"));
        }

        // 8. Urgent filtering
        if (criteria.getUrgent() != null) {
            query.addCriteria(Criteria.where("urgent").is(criteria.getUrgent()));
        }

        // Execute count directly in MongoDB
        long totalElements = mongoTemplate.count(query, Job.class);

        // Sorting & Pagination
        String sortProperty = resolveSortProperty(criteria.getSortBy());
        Sort.Direction direction = "asc".equalsIgnoreCase(criteria.getSortDir()) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(criteria.getPage(), criteria.getSize(), Sort.by(direction, sortProperty));

        query.with(pageable);

        // Execute paginated find directly in MongoDB
        List<Job> jobs = mongoTemplate.find(query, Job.class);

        // Map to sanitized response DTO
        List<JobResponse> content = jobs.stream()
                .map(JobResponse::fromEntity)
                .toList();

        int totalPages = criteria.getSize() > 0
                ? (int) Math.ceil((double) totalElements / criteria.getSize())
                : 0;
        boolean isLast = criteria.getPage() >= totalPages - 1 || totalPages == 0;

        return new PagedResponse<>(content, criteria.getPage(), criteria.getSize(), totalElements, totalPages, isLast);
    }

    private void validateCriteria(JobSearchCriteria criteria) {
        if (criteria.getPage() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Page index must not be less than zero");
        }
        if (criteria.getSize() <= 0 || criteria.getSize() > 100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Page size must be between 1 and 100");
        }
        if (criteria.getMinBudget() != null && criteria.getMinBudget() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Minimum budget cannot be negative");
        }
        if (criteria.getMaxBudget() != null && criteria.getMaxBudget() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Maximum budget cannot be negative");
        }
        if (criteria.getMinBudget() != null && criteria.getMaxBudget() != null
                && criteria.getMinBudget() > criteria.getMaxBudget()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Minimum budget cannot be greater than maximum budget");
        }
    }

    private String resolveSortProperty(String sortBy) {
        if (sortBy != null && ALLOWED_SORT_FIELDS.contains(sortBy)) {
            return sortBy;
        }
        return "createdAt";
    }
}
