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
    private String gstin;
    private String msmeUdyamNumber;
    private String orgScale;
    private String contactName;
    private String contactPhone;
    private String contactEmail;
    private String contactDesignation;
    private String logoUrl;
    private String bannerUrl;
    private Double escrowBalance;
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
        EmployerProfileResponse response = new EmployerProfileResponse(
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
        response.setGstin(profile.getGstin());
        response.setMsmeUdyamNumber(profile.getMsmeUdyamNumber());
        response.setOrgScale(profile.getOrgScale());
        response.setContactName(profile.getContactName());
        response.setContactPhone(profile.getContactPhone());
        response.setContactEmail(profile.getContactEmail());
        response.setContactDesignation(profile.getContactDesignation());
        response.setLogoUrl(profile.getLogoUrl());
        response.setBannerUrl(profile.getBannerUrl());
        response.setEscrowBalance(profile.getEscrowBalance());
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

    public String getGstin() {
        return gstin;
    }

    public void setGstin(String gstin) {
        this.gstin = gstin;
    }

    public String getMsmeUdyamNumber() {
        return msmeUdyamNumber;
    }

    public void setMsmeUdyamNumber(String msmeUdyamNumber) {
        this.msmeUdyamNumber = msmeUdyamNumber;
    }

    public String getOrgScale() {
        return orgScale;
    }

    public void setOrgScale(String orgScale) {
        this.orgScale = orgScale;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public String getContactDesignation() {
        return contactDesignation;
    }

    public void setContactDesignation(String contactDesignation) {
        this.contactDesignation = contactDesignation;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    public String getBannerUrl() {
        return bannerUrl;
    }

    public void setBannerUrl(String bannerUrl) {
        this.bannerUrl = bannerUrl;
    }

    public Double getEscrowBalance() {
        return escrowBalance;
    }

    public void setEscrowBalance(Double escrowBalance) {
        this.escrowBalance = escrowBalance;
    }
}
