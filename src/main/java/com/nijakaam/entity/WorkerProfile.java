package com.nijakaam.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

import java.util.ArrayList;
import java.util.List;

@Document(collection = "worker_profiles")
public class WorkerProfile {

    @Id
    private String id;

    @Indexed(unique = true)
    private String userId;

    private List<String> skills = new ArrayList<>();
    private Integer experienceYears;
    private Double hourlyRate;
    private String bio;
    private String location;
    private boolean available = true;
    private Double rating = 0.0;
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();

    public WorkerProfile() {
    }

    public WorkerProfile(String userId, List<String> skills, Integer experienceYears, Double hourlyRate, String bio,
            String location) {
        this.userId = userId;
        this.skills = skills != null ? new ArrayList<>(skills) : new ArrayList<>();
        this.experienceYears = experienceYears;
        this.hourlyRate = hourlyRate;
        this.bio = bio;
        this.location = location;
        this.available = true;
        this.rating = 0.0;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
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
}
