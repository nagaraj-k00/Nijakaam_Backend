package com.nijakaam.controller;

import com.nijakaam.dto.response.ApiResponse;
import com.nijakaam.entity.JobApplication;
import com.nijakaam.enums.ApplicationStatus;
import com.nijakaam.service.JobApplicationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/job-applications")
public class JobApplicationController {

    private final JobApplicationService jobApplicationService;

    public JobApplicationController(JobApplicationService jobApplicationService) {
        this.jobApplicationService = jobApplicationService;
    }

    // Worker applies for a job
    @PostMapping
    public ResponseEntity<ApiResponse<JobApplication>> applyForJob(@RequestBody JobApplication application) {
        JobApplication createdApplication = jobApplicationService.applyForJob(application);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Application submitted successfully", createdApplication));
    }

    // Get application by ID
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<JobApplication>> getApplicationById(@PathVariable String id) {
        JobApplication application = jobApplicationService.getApplicationById(id);
        return ResponseEntity.ok(ApiResponse.success(application));
    }

    // Get applications with optional filters (workerId, employerId, jobId)
    @GetMapping
    public ResponseEntity<ApiResponse<List<JobApplication>>> getApplications(
            @RequestParam(required = false) String workerId,
            @RequestParam(required = false) String employerId,
            @RequestParam(required = false) String jobId) {

        List<JobApplication> applications;

        int filterCount = 0;

        if (workerId != null)
            filterCount++;
        if (employerId != null)
            filterCount++;
        if (jobId != null)
            filterCount++;

        if (filterCount != 1) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(
                            "Provide exactly one filter: workerId, employerId, or jobId"));
        }

        if (workerId != null) {
            applications = jobApplicationService.getApplicationsByWorker(workerId);
        } else if (employerId != null) {
            applications = jobApplicationService.getApplicationsByEmployer(employerId);
        } else {
            applications = jobApplicationService.getApplicationsByJob(jobId);
        }

        return ResponseEntity.ok(ApiResponse.success(applications));
    }

    // Get applications by worker
    @GetMapping("/worker/{workerId}")
    public ResponseEntity<ApiResponse<List<JobApplication>>> getApplicationsByWorker(@PathVariable String workerId) {
        List<JobApplication> applications = jobApplicationService.getApplicationsByWorker(workerId);
        return ResponseEntity.ok(ApiResponse.success(applications));
    }

    // Get applications by employer
    @GetMapping("/employer/{employerId}")
    public ResponseEntity<ApiResponse<List<JobApplication>>> getApplicationsByEmployer(
            @PathVariable String employerId) {
        List<JobApplication> applications = jobApplicationService.getApplicationsByEmployer(employerId);
        return ResponseEntity.ok(ApiResponse.success(applications));
    }

    // Get applications by job
    @GetMapping("/job/{jobId}")
    public ResponseEntity<ApiResponse<List<JobApplication>>> getApplicationsByJob(@PathVariable String jobId) {
        List<JobApplication> applications = jobApplicationService.getApplicationsByJob(jobId);
        return ResponseEntity.ok(ApiResponse.success(applications));
    }

    // Accept or reject application
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<JobApplication>> updateApplicationStatus(
            @PathVariable String id,
            @RequestParam ApplicationStatus status) {
        JobApplication updatedApplication = jobApplicationService.updateApplicationStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success("Application status updated to " + status, updatedApplication));
    }

    // Withdraw application
    @PatchMapping("/{id}/withdraw")
    public ResponseEntity<ApiResponse<Void>> withdrawApplication(@PathVariable String id) {
        jobApplicationService.withdrawApplication(id);
        return ResponseEntity.ok(ApiResponse.success("Application withdrawn successfully", null));
    }
}
