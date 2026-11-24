package com.nijakaam.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class EmployerProfileRequest {

    @NotBlank(message = "Company name is required")
    @Size(min = 2, max = 150, message = "Company name must be between 2 and 150 characters")
    private String companyName;

    @Size(max = 2000, message = "Company description cannot exceed 2000 characters")
    private String companyDescription;

    @Pattern(regexp = "^$|^(https?://).*", message = "Company website must be a valid URL starting with http:// or https://")
    @Size(max = 255, message = "Company website cannot exceed 255 characters")
    private String companyWebsite;

    @NotBlank(message = "Location is required")
    @Size(min = 2, max = 150, message = "Location must be between 2 and 150 characters")
    private String location;

    @Size(max = 100, message = "Industry cannot exceed 100 characters")
    private String industry;

    // Optional field: backend ignores client-supplied userId to prevent ownership hijacking
    private String userId;

    public EmployerProfileRequest() {
    }

    public EmployerProfileRequest(String companyName, String companyDescription, String companyWebsite, String location, String industry) {
        this.companyName = companyName;
        this.companyDescription = companyDescription;
        this.companyWebsite = companyWebsite;
        this.location = location;
        this.industry = industry;
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

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
