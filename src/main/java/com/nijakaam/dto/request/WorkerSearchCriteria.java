package com.nijakaam.dto.request;

public class WorkerSearchCriteria {

    private String skill;
    private String location;
    private Integer minExperience;
    private Integer maxExperience;
    private Double minRate;
    private Double maxRate;
    private Boolean available;
    private int page = 0;
    private int size = 10;
    private String sortBy = "rating";
    private String sortDir = "desc";

    public WorkerSearchCriteria() {
    }

    public WorkerSearchCriteria(String skill, String location, Integer minExperience, Integer maxExperience,
                                Double minRate, Double maxRate, Boolean available,
                                int page, int size, String sortBy, String sortDir) {
        this.skill = skill;
        this.location = location;
        this.minExperience = minExperience;
        this.maxExperience = maxExperience;
        this.minRate = minRate;
        this.maxRate = maxRate;
        this.available = available;
        this.page = page;
        this.size = size;
        this.sortBy = sortBy != null ? sortBy : "rating";
        this.sortDir = sortDir != null ? sortDir : "desc";
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

    public Integer getMinExperience() {
        return minExperience;
    }

    public void setMinExperience(Integer minExperience) {
        this.minExperience = minExperience;
    }

    public Integer getMaxExperience() {
        return maxExperience;
    }

    public void setMaxExperience(Integer maxExperience) {
        this.maxExperience = maxExperience;
    }

    public Double getMinRate() {
        return minRate;
    }

    public void setMinRate(Double minRate) {
        this.minRate = minRate;
    }

    public Double getMaxRate() {
        return maxRate;
    }

    public void setMaxRate(Double maxRate) {
        this.maxRate = maxRate;
    }

    public Boolean getAvailable() {
        return available;
    }

    public void setAvailable(Boolean available) {
        this.available = available;
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
