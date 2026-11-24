package com.nijakaam.dto.request;

import com.nijakaam.enums.JobStatus;

public class JobSearchCriteria {

    private String keyword;
    private String skill;
    private String location;
    private Double minBudget;
    private Double maxBudget;
    private JobStatus status;
    private int page = 0;
    private int size = 10;
    private String sortBy = "createdAt";
    private String sortDir = "desc";

    public JobSearchCriteria() {
    }

    public JobSearchCriteria(String keyword, String skill, String location,
                             Double minBudget, Double maxBudget, JobStatus status,
                             int page, int size, String sortBy, String sortDir) {
        this.keyword = keyword;
        this.skill = skill;
        this.location = location;
        this.minBudget = minBudget;
        this.maxBudget = maxBudget;
        this.status = status;
        this.page = page;
        this.size = size;
        this.sortBy = sortBy != null ? sortBy : "createdAt";
        this.sortDir = sortDir != null ? sortDir : "desc";
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getSkill() {
        return skill;
    }

    public void setSkill(String skill) {
        this.skill = skill;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Double getMinBudget() {
        return minBudget;
    }

    public void setMinBudget(Double minBudget) {
        this.minBudget = minBudget;
    }

    public Double getMaxBudget() {
        return maxBudget;
    }

    public void setMaxBudget(Double maxBudget) {
        this.maxBudget = maxBudget;
    }

    public JobStatus getStatus() {
        return status;
    }

    public void setStatus(JobStatus status) {
        this.status = status;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public String getSortBy() {
        return sortBy;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    public String getSortDir() {
        return sortDir;
    }

    public void setSortDir(String sortDir) {
        this.sortDir = sortDir;
    }
}
