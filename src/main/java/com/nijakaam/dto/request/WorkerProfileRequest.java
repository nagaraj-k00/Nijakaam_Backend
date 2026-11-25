package com.nijakaam.dto.request;

import jakarta.validation.constraints.*;

import java.util.ArrayList;
import java.util.List;

public class WorkerProfileRequest {

    @NotNull(message = "Skills are required")
    @Size(min = 1, max = 30, message = "Provide between 1 and 30 skills")
    private List<@NotBlank(message = "Skill cannot be blank") @Size(max = 50, message = "Skill name cannot exceed 50 characters") String> skills;

    @NotNull(message = "Experience years is required")
    @Min(value = 0, message = "Experience years cannot be negative")
    @Max(value = 70, message = "Experience years cannot exceed 70")
    private Integer experienceYears;

    @NotNull(message = "Hourly rate is required")
    @DecimalMin(value = "0.0", message = "Hourly rate cannot be negative")
    @Digits(integer = 8, fraction = 2, message = "Hourly rate can have up to 2 decimal places")
    private Double hourlyRate;

    @Size(max = 2000, message = "Bio cannot exceed 2000 characters")
    private String bio;

    private Double dailyRate;
    private List<String> certifications;
    private String preferredShift;
    private String avatarUrl;

    @NotBlank(message = "Location is required")
    @Size(min = 2, max = 150, message = "Location must be between 2 and 150 characters")
    private String location;

    private Boolean available = true;

    // Optional field to capture if a client passes a userId/workerId; backend must not trust this for ownership
    private String userId;

    public WorkerProfileRequest() {
    }

    public WorkerProfileRequest(List<String> skills, Integer experienceYears, Double hourlyRate, String bio, String location, Boolean available) {
        this.skills = skills != null ? new ArrayList<>(skills) : new ArrayList<>();
        this.experienceYears = experienceYears;
        this.hourlyRate = hourlyRate;
        this.bio = bio;
        this.location = location;
        this.available = available != null ? available : true;
    }

    public List<String> getSkills() {
        return skills;
    }

    public void setSkills(List<String> skills) {
        this.skills = skills;
    }

    public Integer getExperienceYears() {
        return experienceYears;
    }

    public void setExperienceYears(Integer experienceYears) {
        this.experienceYears = experienceYears;
    }

    public Double getHourlyRate() {
        return hourlyRate;
    }

    public void setHourlyRate(Double hourlyRate) {
        this.hourlyRate = hourlyRate;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Boolean getAvailable() {
        return available;
    }

    public void setAvailable(Boolean available) {
        this.available = available;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Double getDailyRate() {
        return dailyRate;
    }

    public void setDailyRate(Double dailyRate) {
        this.dailyRate = dailyRate;
    }

    public List<String> getCertifications() {
        return certifications;
    }

    public void setCertifications(List<String> certifications) {
        this.certifications = certifications;
    }

    public String getPreferredShift() {
        return preferredShift;
    }

    public void setPreferredShift(String preferredShift) {
        this.preferredShift = preferredShift;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }
}
