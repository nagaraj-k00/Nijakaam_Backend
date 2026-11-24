package com.nijakaam.controller;

import com.nijakaam.dto.request.WorkerProfileRequest;
import com.nijakaam.entity.WorkerProfile;
import com.nijakaam.exception.GlobalExceptionHandler;
import com.nijakaam.exception.ResourceNotFoundException;
import com.nijakaam.service.WorkerProfileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class WorkerProfileControllerSecurityTest {

    private MockMvc mockMvc;

    @Mock
    private WorkerProfileService workerProfileService;

    @InjectMocks
    private WorkerProfileController workerProfileController;

    private WorkerProfile sampleProfile;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(workerProfileController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        sampleProfile = new WorkerProfile("workerA_id", List.of("Plumbing", "Carpentry"), 4, 30.0, "Skilled worker", "Bangalore");
        sampleProfile.setId("profileA_id");
    }

    @Test
    @DisplayName("POST /api/worker-profiles returns 201 Created on valid request (canonical creation endpoint)")
    void createProfile_Success_Returns201() throws Exception {
        when(workerProfileService.createProfile(any(WorkerProfileRequest.class)))
                .thenReturn(sampleProfile);

        String jsonBody = """
                {
                    "skills": ["Plumbing", "Carpentry"],
                    "experienceYears": 4,
                    "hourlyRate": 30.0,
                    "bio": "Skilled worker",
                    "location": "Bangalore",
                    "available": true
                }
                """;

        mockMvc.perform(post("/api/worker-profiles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.userId").value("workerA_id"))
                .andExpect(jsonPath("$.data.skills[0]").value("Plumbing"))
                .andExpect(jsonPath("$.data.hourlyRate").value(30.0));
    }

    @Test
    @DisplayName("Scenario 10: Invalid profile data returns HTTP 400 Bad Request with validation errors")
    void createProfile_InvalidData_Returns400() throws Exception {
        // Missing skills, negative experience, negative rate, missing location
        String invalidJson = """
                {
                    "skills": [],
                    "experienceYears": -2,
                    "hourlyRate": -10.0,
                    "location": ""
                }
                """;

        mockMvc.perform(post("/api/worker-profiles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.data.skills").exists())
                .andExpect(jsonPath("$.data.experienceYears").exists())
                .andExpect(jsonPath("$.data.hourlyRate").exists())
                .andExpect(jsonPath("$.data.location").exists());

        verify(workerProfileService, never()).createProfile(any(WorkerProfileRequest.class));
    }

    @Test
    @DisplayName("POST /api/worker-profiles returns 409 Conflict on duplicate profile")
    void createProfile_Duplicate_Returns409() throws Exception {
        when(workerProfileService.createProfile(any(WorkerProfileRequest.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.CONFLICT, "Worker profile already exists for this user"));

        String jsonBody = """
                {
                    "skills": ["Plumbing"],
                    "experienceYears": 2,
                    "hourlyRate": 20.0,
                    "location": "Bangalore"
                }
                """;

        mockMvc.perform(post("/api/worker-profiles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Worker profile already exists for this user"));
    }

    @Test
    @DisplayName("GET /api/worker-profiles/me returns 200 OK with authenticated worker's profile")
    void getMyProfile_Returns200() throws Exception {
        when(workerProfileService.getMyProfile()).thenReturn(sampleProfile);

        mockMvc.perform(get("/api/worker-profiles/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.userId").value("workerA_id"));
    }

    @Test
    @DisplayName("GET /api/worker-profiles/{userId} returns 200 OK for owner")
    void getProfile_ByOwner_Returns200() throws Exception {
        when(workerProfileService.getProfileByUserId("workerA_id")).thenReturn(sampleProfile);

        mockMvc.perform(get("/api/worker-profiles/workerA_id"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.userId").value("workerA_id"));
    }

    @Test
    @DisplayName("GET /api/worker-profiles/{userId} returns 403 Forbidden when unauthorized")
    void getProfile_Unauthorized_Returns403() throws Exception {
        when(workerProfileService.getProfileByUserId("workerA_id"))
                .thenThrow(new AccessDeniedException("You are not authorized to view this profile"));

        mockMvc.perform(get("/api/worker-profiles/workerA_id"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("You are not authorized to view this profile"));
    }

    @Test
    @DisplayName("GET /api/worker-profiles/{userId} returns 404 Not Found when profile does not exist")
    void getProfile_NotFound_Returns404() throws Exception {
        when(workerProfileService.getProfileByUserId("non_existent"))
                .thenThrow(new ResourceNotFoundException("Worker profile not found for user id: non_existent"));

        mockMvc.perform(get("/api/worker-profiles/non_existent"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Worker profile not found for user id: non_existent"));
    }

    @Test
    @DisplayName("PUT /api/worker-profiles/{userId} returns 403 Forbidden when another worker tries to update")
    void updateProfile_Unauthorized_Returns403() throws Exception {
        when(workerProfileService.updateProfile(eq("workerA_id"), any(WorkerProfileRequest.class)))
                .thenThrow(new AccessDeniedException("You are not authorized to modify this profile"));

        String jsonBody = """
                {
                    "skills": ["Electrical"],
                    "experienceYears": 3,
                    "hourlyRate": 30.0,
                    "location": "Bangalore"
                }
                """;

        mockMvc.perform(put("/api/worker-profiles/workerA_id")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("You are not authorized to modify this profile"));
    }

    @Test
    @DisplayName("DELETE /api/worker-profiles/{userId} returns 200 OK when owner deletes profile")
    void deleteProfile_ByOwner_Returns200() throws Exception {
        doNothing().when(workerProfileService).deleteProfile("workerA_id");

        mockMvc.perform(delete("/api/worker-profiles/workerA_id"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Worker profile deleted successfully"));

        verify(workerProfileService, times(1)).deleteProfile("workerA_id");
    }

    @Test
    @DisplayName("DELETE /api/worker-profiles/{userId} returns 403 Forbidden when unauthorized user tries to delete")
    void deleteProfile_Unauthorized_Returns403() throws Exception {
        doThrow(new AccessDeniedException("You are not authorized to delete this profile"))
                .when(workerProfileService).deleteProfile("workerA_id");

        mockMvc.perform(delete("/api/worker-profiles/workerA_id"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("You are not authorized to delete this profile"));
    }
}
