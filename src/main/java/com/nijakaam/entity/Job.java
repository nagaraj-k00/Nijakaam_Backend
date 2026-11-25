package com.nijakaam.entity;

import com.nijakaam.enums.JobStatus;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "jobs")
@CompoundIndex(name = "job_status_created_idx", def = "{'status': 1, 'createdAt': -1}")
@CompoundIndex(name = "job_status_location_idx", def = "{'status': 1, 'location': 1}")
public class Job {

    @Id
    private String id;
    private String title;
    private String description;
    private String employerId;
    private List<String> skillsRequired = new ArrayList<>();
    private Double budget;
    private JobStatus status = JobStatus.OPEN;
    private String location;
    private String category;
    private Integer openings = 1;
    private String engagementModel = "Full-Time";
    private String experienceLevel;
    private String wageCadence = "MONTHLY";
    private String shiftTiming;
    private boolean urgent = false;
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();

    public Job() {
    }

    public Job(String title, String description, String employerId, List<String> skillsRequired, Double budget,
            String location) {
        this.title = title;
        this.description = description;
        this.employerId = employerId;
        this.skillsRequired = skillsRequired != null
                ? new ArrayList<>(skillsRequired)
                : new ArrayList<>();
        this.budget = budget;
        this.location = location;
        this.status = JobStatus.OPEN;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Integer getOpenings() {
        return openings;
    }

    public void setOpenings(Integer openings) {
        this.openings = openings;
    }

    public String getEngagementModel() {
        return engagementModel;
    }

    public void setEngagementModel(String engagementModel) {
        this.engagementModel = engagementModel;
    }

    public String getExperienceLevel() {
        return experienceLevel;
    }

    public void setExperienceLevel(String experienceLevel) {
        this.experienceLevel = experienceLevel;
    }

    public String getWageCadence() {
        return wageCadence;
    }

    public void setWageCadence(String wageCadence) {
        this.wageCadence = wageCadence;
    }

    public String getShiftTiming() {
        return shiftTiming;
    }

    public void setShiftTiming(String shiftTiming) {
        this.shiftTiming = shiftTiming;
    }

    public boolean isUrgent() {
        return urgent;
    }

    public void setUrgent(boolean urgent) {
        this.urgent = urgent;
    }
}
