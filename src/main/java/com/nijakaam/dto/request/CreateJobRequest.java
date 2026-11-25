
package com.nijakaam.dto.request;

import jakarta.validation.constraints.*;

import java.util.List;

public class CreateJobRequest {

    @NotBlank(message = "Job title is required")
    @Size(min = 3, max = 100, message = "Job title must be between 3 and 100 characters")
    private String title;

    @NotBlank(message = "Job description is required")
    @Size(min = 20, max = 3000, message = "Job description must be between 20 and 3000 characters")
    private String description;

    @NotNull(message = "Skills are required")
    @Size(min = 1, max = 20, message = "Provide between 1 and 20 skills")
    private List<@NotBlank(message = "Skill cannot be blank") String> skillsRequired;

    @NotNull(message = "Budget is required")
    @DecimalMin(value = "1.0", message = "Budget must be greater than zero")
    @Digits(integer = 10, fraction = 2, message = "Budget can have up to 2 decimal places")
    private Double budget;

    @NotBlank(message = "Location is required")
    @Size(max = 150, message = "Location cannot exceed 150 characters")
    private String location;

    private String category;
    private Integer openings;
    private String engagementModel;
    private String experienceLevel;
    private String wageCadence;
    private String shiftTiming;
    private Boolean urgent;

    public CreateJobRequest() {
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

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
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

    public Boolean getUrgent() {
        return urgent;
    }

    public void setUrgent(Boolean urgent) {
        this.urgent = urgent;
    }
}
