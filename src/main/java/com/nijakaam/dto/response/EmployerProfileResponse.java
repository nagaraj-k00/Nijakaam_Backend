package com.nijakaam.dto.response;

import com.nijakaam.entity.EmployerProfile;

import java.time.LocalDateTime;

public class EmployerProfileResponse {

    private String id;
    private String userId;
    private String companyName;
    private String companyDescription;
    private String companyWebsite;
    private String location;
    private String industry;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public EmployerProfileResponse() {
    }

    public EmployerProfileResponse(String id, String userId, String companyName, String companyDescription,
                                   String companyWebsite, String location, String industry,
                                   LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.userId = userId;
        this.companyName = companyName;
        this.companyDescription = companyDescription;
        this.companyWebsite = companyWebsite;
        this.location = location;
        this.industry = industry;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static EmployerProfileResponse fromEntity(EmployerProfile profile) {
        if (profile == null) {
            return null;
        }
        return new EmployerProfileResponse(
                profile.getId(),
                profile.getUserId(),
                profile.getCompanyName(),
                profile.getCompanyDescription(),
                profile.getCompanyWebsite(),
                profile.getLocation(),
                profile.getIndustry(),
                profile.getCreatedAt(),
                profile.getUpdatedAt()
        );
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

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getCompanyDescription() {
        return companyDescription;
    }

    public void setCompanyDescription(String companyDescription) {
        this.companyDescription = companyDescription;
    }

    public String getCompanyWebsite() {
        return companyWebsite;
    }

    public void setCompanyWebsite(String companyWebsite) {
        this.companyWebsite = companyWebsite;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getIndustry() {
        return industry;
    }

    public void setIndustry(String industry) {
        this.industry = industry;
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
