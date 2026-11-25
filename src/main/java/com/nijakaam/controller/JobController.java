
package com.nijakaam.controller;

import com.nijakaam.dto.request.CreateJobRequest;
import com.nijakaam.dto.request.JobSearchCriteria;
import com.nijakaam.dto.response.ApiResponse;
import com.nijakaam.dto.response.JobResponse;
import com.nijakaam.dto.response.PagedResponse;
import com.nijakaam.entity.Job;
import com.nijakaam.enums.JobStatus;
import com.nijakaam.service.JobService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/jobs")
public class JobController {

    private final JobService jobService;

    public JobController(JobService jobService) {
        this.jobService = jobService;
    }

    // Create Job
    @PostMapping
    public ResponseEntity<ApiResponse<Job>> createJob(
            @Valid @RequestBody CreateJobRequest request) {

        Job job = mapToJob(request);

        Job createdJob = jobService.createJob(job);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Job created successfully", createdJob));
    }

    // Search Jobs
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PagedResponse<JobResponse>>> searchJobs(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String skill,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) Double minBudget,
            @RequestParam(required = false) Double maxBudget,
            @RequestParam(required = false) JobStatus status,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String engagementModel,
            @RequestParam(required = false) Boolean urgent,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        JobSearchCriteria criteria = new JobSearchCriteria(
                keyword, skill, location, minBudget, maxBudget, status, page, size, sortBy, sortDir);
        criteria.setCategory(category);
        criteria.setEngagementModel(engagementModel);
        criteria.setUrgent(urgent);

        PagedResponse<JobResponse> result = jobService.searchJobs(criteria);

        return ResponseEntity.ok(ApiResponse.success("Jobs retrieved successfully", result));
    }

    // Get Job By ID
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Job>> getJobById(@PathVariable String id) {

        Job job = jobService.getJobById(id);

        return ResponseEntity.ok(ApiResponse.success(job));
    }

    // Get All Jobs / Filter Jobs
    @GetMapping
    public ResponseEntity<ApiResponse<List<Job>>> getAllJobs(
            @RequestParam(required = false) String employerId,
            @RequestParam(required = false) JobStatus status) {

        List<Job> jobs;

        if (employerId != null && status != null) {
            jobs = jobService.getJobsByEmployerId(employerId)
                    .stream()
                    .filter(job -> job.getStatus() == status)
                    .toList();

        } else if (employerId != null) {
            jobs = jobService.getJobsByEmployerId(employerId);

        } else if (status != null) {
            jobs = jobService.getJobsByStatus(status);

        } else {
            jobs = jobService.getAllJobs();
        }

        return ResponseEntity.ok(ApiResponse.success(jobs));
    }

    // Update Job Details
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Job>> updateJob(
            @PathVariable String id,
            @Valid @RequestBody CreateJobRequest request) {

        Job job = mapToJob(request);

        Job updatedJob = jobService.updateJob(id, job);

        return ResponseEntity.ok(
                ApiResponse.success("Job updated successfully", updatedJob));
    }

    // Update Job Status
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<Job>> updateJobStatus(
            @PathVariable String id,
            @RequestParam JobStatus status) {

        Job updatedJob = jobService.updateJobStatus(id, status);

        return ResponseEntity.ok(
                ApiResponse.success("Job status updated", updatedJob));
    }

    // Delete Job
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteJob(@PathVariable String id) {

        jobService.deleteJob(id);

        return ResponseEntity.ok(
                ApiResponse.success("Job deleted successfully", null));
    }

    // Map Request DTO to Job Entity
    private Job mapToJob(CreateJobRequest request) {

        Job job = new Job();

        job.setTitle(request.getTitle());
        job.setDescription(request.getDescription());
        job.setSkillsRequired(request.getSkillsRequired());
        job.setBudget(request.getBudget());
        job.setLocation(request.getLocation());
        job.setCategory(request.getCategory());
        if (request.getOpenings() != null) {
            job.setOpenings(request.getOpenings());
        }
        if (request.getEngagementModel() != null) {
            job.setEngagementModel(request.getEngagementModel());
        }
        job.setExperienceLevel(request.getExperienceLevel());
        if (request.getWageCadence() != null) {
            job.setWageCadence(request.getWageCadence());
        }
        job.setShiftTiming(request.getShiftTiming());
        if (request.getUrgent() != null) {
            job.setUrgent(request.getUrgent());
        }

        return job;
    }
}
