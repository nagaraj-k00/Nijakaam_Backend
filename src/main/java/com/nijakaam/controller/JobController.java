package com.nijakaam.controller;

import com.nijakaam.dto.response.ApiResponse;
import com.nijakaam.entity.Job;
import com.nijakaam.enums.JobStatus;
import com.nijakaam.service.JobService;
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
    public ResponseEntity<ApiResponse<Job>> createJob(@RequestBody Job job) {

        Job createdJob = jobService.createJob(job);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Job created successfully", createdJob));
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
            @RequestBody Job job) {

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
}