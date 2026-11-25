package com.nijakaam.entity;

import com.nijakaam.enums.ApplicationStatus;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "job_applications")
@CompoundIndex(
        name = "unique_worker_job_application",
        def = "{'workerId': 1, 'jobId': 1}",
        unique = true
)
public class JobApplication {

    @Id
    private String id;

    private String jobId;
    private String workerId;
    private String employerId;

    private String applicationMessage;

    private ApplicationStatus status = ApplicationStatus.PENDING;

    // Enriched worker & job details for employer visibility
    private String workerName;
    private String workerPhone;
    private String workerEmail;
    private String workerLocation;
    private List<String> workerSkills = new ArrayList<>();
    private Double workerRating;
    private String workerAvatarUrl;
    private Integer experienceYears;
    private String jobTitle;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();

    public JobApplication() {
    }

    public JobApplication(String jobId, String workerId, String employerId,
                          String applicationMessage) {

        this.jobId = jobId;
        this.workerId = workerId;
        this.employerId = employerId;
        this.applicationMessage = applicationMessage;
        this.status = ApplicationStatus.PENDING;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
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

    public String getWorkerId() {
        return workerId;
    }

    public void setWorkerId(String workerId) {
        this.workerId = workerId;
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

    public Integer getExperienceYears() {
        return experienceYears;
    }

    public void setExperienceYears(Integer experienceYears) {
        this.experienceYears = experienceYears;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }
}
