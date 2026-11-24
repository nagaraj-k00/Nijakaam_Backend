
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
}
