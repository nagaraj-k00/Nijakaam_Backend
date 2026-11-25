package com.nijakaam.dto.response;

import com.nijakaam.entity.WorkerProfile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class WorkerProfileResponse {

    private String id;
    private String userId;
    private List<String> skills = new ArrayList<>();
    private Integer experienceYears;
    private Double hourlyRate;
    private Double dailyRate;
    private List<String> certifications = new ArrayList<>();
    private String preferredShift;
    private String avatarUrl;
    private String bio;
    private String location;
    private boolean available;
    private Double rating;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public WorkerProfileResponse() {
    }

    public WorkerProfileResponse(String id, String userId, List<String> skills, Integer experienceYears,
                                 Double hourlyRate, String bio, String location, boolean available,
                                 Double rating, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.userId = userId;
        this.skills = skills != null ? new ArrayList<>(skills) : new ArrayList<>();
        this.experienceYears = experienceYears;
        this.hourlyRate = hourlyRate;
        this.bio = bio;
        this.location = location;
        this.available = available;
        this.rating = rating;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static WorkerProfileResponse fromEntity(WorkerProfile profile) {
        if (profile == null) {
            return null;
        }
        WorkerProfileResponse response = new WorkerProfileResponse(
                profile.getId(),
                profile.getUserId(),
                profile.getSkills(),
                profile.getExperienceYears(),
                profile.getHourlyRate(),
                profile.getBio(),
                profile.getLocation(),
                profile.isAvailable(),
                profile.getRating(),
                profile.getCreatedAt(),
                profile.getUpdatedAt()
        );
        response.setDailyRate(profile.getDailyRate());
        if (profile.getCertifications() != null) {
            response.setCertifications(new ArrayList<>(profile.getCertifications()));
        }
        response.setPreferredShift(profile.getPreferredShift());
        response.setAvatarUrl(profile.getAvatarUrl());
        return response;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
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

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
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
        this.certifications = certifications != null ? certifications : new ArrayList<>();
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
