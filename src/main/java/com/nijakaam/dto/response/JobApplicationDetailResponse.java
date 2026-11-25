package com.nijakaam.dto.response;

import com.nijakaam.entity.JobApplication;
import com.nijakaam.enums.ApplicationStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class JobApplicationDetailResponse {

    private String id;
    private String jobId;
    private String jobTitle;
    private String workerId;
    private String workerName;
    private String workerPhone;
    private String workerEmail;
    private String workerLocation;
    private Integer experienceYears;
    private List<String> workerSkills = new ArrayList<>();
    private Double workerRating;
    private String workerAvatarUrl;
    private String employerId;
    private String applicationMessage;
    private ApplicationStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public JobApplicationDetailResponse() {
    }

    public static JobApplicationDetailResponse fromEntity(JobApplication app) {
        if (app == null) {
            return null;
        }
        JobApplicationDetailResponse dto = new JobApplicationDetailResponse();
        dto.setId(app.getId());
        dto.setJobId(app.getJobId());
        dto.setJobTitle(app.getJobTitle());
        dto.setWorkerId(app.getWorkerId());
        dto.setWorkerName(app.getWorkerName());
        dto.setWorkerPhone(app.getWorkerPhone());
        dto.setWorkerEmail(app.getWorkerEmail());
        dto.setWorkerLocation(app.getWorkerLocation());
        dto.setExperienceYears(app.getExperienceYears());
        if (app.getWorkerSkills() != null) {
            dto.setWorkerSkills(new ArrayList<>(app.getWorkerSkills()));
        }
        dto.setWorkerRating(app.getWorkerRating());
        dto.setWorkerAvatarUrl(app.getWorkerAvatarUrl());
        dto.setEmployerId(app.getEmployerId());
        dto.setApplicationMessage(app.getApplicationMessage());
        dto.setStatus(app.getStatus());
        dto.setCreatedAt(app.getCreatedAt());
        dto.setUpdatedAt(app.getUpdatedAt());
        return dto;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    public String getWorkerId() {
        return workerId;
    }

    public void setWorkerId(String workerId) {
        this.workerId = workerId;
    }

    public String getWorkerName() {
        return workerName;
    }

    public void setWorkerName(String workerName) {
        this.workerName = workerName;
    }

    public String getWorkerPhone() {
        return workerPhone;
    }

    public void setWorkerPhone(String workerPhone) {
        this.workerPhone = workerPhone;
    }

    public String getWorkerEmail() {
        return workerEmail;
    }

    public void setWorkerEmail(String workerEmail) {
        this.workerEmail = workerEmail;
    }

    public String getWorkerLocation() {
        return workerLocation;
    }

    public void setWorkerLocation(String workerLocation) {
        this.workerLocation = workerLocation;
    }

    public Integer getExperienceYears() {
        return experienceYears;
    }

    public void setExperienceYears(Integer experienceYears) {
        this.experienceYears = experienceYears;
    }

    public List<String> getWorkerSkills() {
        return workerSkills;
    }

    public void setWorkerSkills(List<String> workerSkills) {
        this.workerSkills = workerSkills;
    }

    public Double getWorkerRating() {
        return workerRating;
    }

    public void setWorkerRating(Double workerRating) {
        this.workerRating = workerRating;
    }

    public String getWorkerAvatarUrl() {
        return workerAvatarUrl;
    }

    public void setWorkerAvatarUrl(String workerAvatarUrl) {
        this.workerAvatarUrl = workerAvatarUrl;
    }

    public String getEmployerId() {
        return employerId;
    }

    public void setEmployerId(String employerId) {
        this.employerId = employerId;
    }

    public String getApplicationMessage() {
        return applicationMessage;
    }

    public void setApplicationMessage(String applicationMessage) {
        this.applicationMessage = applicationMessage;
    }

    public ApplicationStatus getStatus() {
        return status;
    }

    public void setStatus(ApplicationStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
