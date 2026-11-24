package com.nijakaam.dto.response;

import com.nijakaam.entity.Job;
import com.nijakaam.enums.JobStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class JobResponse {

    private String id;
    private String title;
    private String description;
    private String employerId;
    private List<String> skillsRequired = new ArrayList<>();
    private Double budget;
    private JobStatus status;
    private String location;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public JobResponse() {
    }

    public JobResponse(String id, String title, String description, String employerId,
                       List<String> skillsRequired, Double budget, JobStatus status,
                       String location, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.employerId = employerId;
        this.skillsRequired = skillsRequired != null ? new ArrayList<>(skillsRequired) : new ArrayList<>();
        this.budget = budget;
        this.status = status;
        this.location = location;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static JobResponse fromEntity(Job job) {
        if (job == null) {
            return null;
        }
        return new JobResponse(
                job.getId(),
                job.getTitle(),
                job.getDescription(),
                job.getEmployerId(),
                job.getSkillsRequired(),
                job.getBudget(),
                job.getStatus(),
                job.getLocation(),
                job.getCreatedAt(),
                job.getUpdatedAt()
        );
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getEmployerId() {
        return employerId;
    }

    public void setEmployerId(String employerId) {
        this.employerId = employerId;
    }

    public List<String> getSkillsRequired() {
        return skillsRequired;
    }

    public void setSkillsRequired(List<String> skillsRequired) {
        this.skillsRequired = skillsRequired;
    }

    public Double getBudget() {
        return budget;
    }

    public void setBudget(Double budget) {
        this.budget = budget;
    }

    public JobStatus getStatus() {
        return status;
    }

    public void setStatus(JobStatus status) {
        this.status = status;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
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
