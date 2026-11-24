package com.nijakaam.controller;

import com.nijakaam.dto.response.ApiResponse;
import com.nijakaam.entity.Job;
import com.nijakaam.enums.JobStatus;
import com.nijakaam.exception.GlobalExceptionHandler;
import com.nijakaam.exception.ResourceNotFoundException;
import com.nijakaam.service.JobService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class JobControllerAuthorizationTest {

    private MockMvc mockMvc;

    @Mock
    private JobService jobService;

    @InjectMocks
    private JobController jobController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(jobController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("DELETE /api/jobs/{id} returns 200 OK when Employer A deletes their own job")
    void deleteJob_ByOwner_Returns200() throws Exception {
        doNothing().when(jobService).deleteJob("jobA_id");

        mockMvc.perform(delete("/api/jobs/jobA_id"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Job deleted successfully"));

        verify(jobService, times(1)).deleteJob("jobA_id");
    }

    @Test
    @DisplayName("DELETE /api/jobs/{id} returns 403 FORBIDDEN when Employer B attempts to delete Employer A's job")
    void deleteJob_ByUnauthorizedEmployer_Returns403() throws Exception {
        doThrow(new AccessDeniedException("You are not authorized to modify this job"))
                .when(jobService).deleteJob("jobA_id");

        mockMvc.perform(delete("/api/jobs/jobA_id"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("You are not authorized to modify this job"));

        verify(jobService, times(1)).deleteJob("jobA_id");
    }

    @Test
    @DisplayName("DELETE /api/jobs/{id} returns 404 NOT FOUND when job does not exist")
    void deleteJob_NotFound_Returns404() throws Exception {
        doThrow(new ResourceNotFoundException("Job not found with id: non_existent"))
                .when(jobService).deleteJob("non_existent");

        mockMvc.perform(delete("/api/jobs/non_existent"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Job not found with id: non_existent"));
    }

    @Test
    @DisplayName("PUT /api/jobs/{id} returns 403 FORBIDDEN when Employer B attempts to update Employer A's job")
    void updateJob_ByUnauthorizedEmployer_Returns403() throws Exception {
        when(jobService.updateJob(eq("jobA_id"), any(Job.class)))
                .thenThrow(new AccessDeniedException("You are not authorized to modify this job"));

        String requestBody = """
                {
                    "title": "Hacked Title",
                    "description": "Hacked Description with more than twenty characters",
                    "skillsRequired": ["Plumbing"],
                    "budget": 999.0,
                    "location": "Bangalore"
                }
                """;

        mockMvc.perform(put("/api/jobs/jobA_id")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("You are not authorized to modify this job"));
    }

    @Test
    @DisplayName("PATCH /api/jobs/{id}/status returns 403 FORBIDDEN when Employer B attempts to change status")
    void updateJobStatus_ByUnauthorizedEmployer_Returns403() throws Exception {
        when(jobService.updateJobStatus(eq("jobA_id"), eq(JobStatus.COMPLETED)))
                .thenThrow(new AccessDeniedException("You are not authorized to modify this job"));

        mockMvc.perform(patch("/api/jobs/jobA_id/status")
                        .param("status", "COMPLETED"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("You are not authorized to modify this job"));
    }

    @Test
    @DisplayName("PUT /api/jobs/{id} returns 200 OK when Employer A updates their own job")
    void updateJob_ByOwner_Returns200() throws Exception {
        Job updated = new Job("New Title", "New Desc", "empA_id", List.of(), 500.0, "Bangalore");
        when(jobService.updateJob(eq("jobA_id"), any(Job.class))).thenReturn(updated);

        String requestBody = """
                {
                    "title": "New Title",
                    "description": "New Valid Description with more than twenty characters",
                    "skillsRequired": ["Plumbing"],
                    "budget": 500.0,
                    "location": "Bangalore"
                }
                """;

        mockMvc.perform(put("/api/jobs/jobA_id")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("New Title"));
    }
}
