package com.nijakaam.controller;

import com.nijakaam.dto.request.EmployerProfileRequest;
import com.nijakaam.entity.EmployerProfile;
import com.nijakaam.exception.GlobalExceptionHandler;
import com.nijakaam.exception.ResourceNotFoundException;
import com.nijakaam.service.EmployerProfileService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class EmployerProfileControllerSecurityTest {

    private MockMvc mockMvc;

    @Mock
    private EmployerProfileService employerProfileService;

    @InjectMocks
    private EmployerProfileController employerProfileController;

    private EmployerProfile sampleProfile;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(employerProfileController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        sampleProfile = new EmployerProfile(
                "employerA_id",
                "Acme Corp",
                "Leading logistics provider",
                "https://acme.com",
                "Bangalore",
                "Logistics"
        );
        sampleProfile.setId("profileA_id");
    }

    @Test
    @DisplayName("POST /api/employer-profiles returns 201 Created on valid request (canonical creation endpoint)")
    void createProfile_Success_Returns201() throws Exception {
        when(employerProfileService.createProfile(any(EmployerProfileRequest.class)))
                .thenReturn(sampleProfile);

        String jsonBody = """
                {
                    "companyName": "Acme Corp",
                    "companyDescription": "Leading logistics provider",
                    "companyWebsite": "https://acme.com",
                    "location": "Bangalore",
                    "industry": "Logistics"
                }
                """;

        mockMvc.perform(post("/api/employer-profiles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.userId").value("employerA_id"))
                .andExpect(jsonPath("$.data.companyName").value("Acme Corp"))
                .andExpect(jsonPath("$.data.location").value("Bangalore"));
    }

    @Test
    @DisplayName("Scenario 10: Invalid profile data returns HTTP 400 Bad Request with validation errors")
    void createProfile_InvalidData_Returns400() throws Exception {
        // Missing companyName, missing location, invalid URL format
        String invalidJson = """
                {
                    "companyName": "",
                    "companyDescription": "Some description",
                    "companyWebsite": "ftp://invalid-url.com",
                    "location": ""
                }
                """;

        mockMvc.perform(post("/api/employer-profiles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.data.companyName").exists())
                .andExpect(jsonPath("$.data.location").exists())
                .andExpect(jsonPath("$.data.companyWebsite").exists());

        verify(employerProfileService, never()).createProfile(any(EmployerProfileRequest.class));
    }

    @Test
    @DisplayName("POST /api/employer-profiles returns 409 Conflict on duplicate profile")
    void createProfile_Duplicate_Returns409() throws Exception {
        when(employerProfileService.createProfile(any(EmployerProfileRequest.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.CONFLICT, "Employer profile already exists for this user"));

        String jsonBody = """
                {
                    "companyName": "Acme Corp",
                    "companyDescription": "Tech",
                    "companyWebsite": "https://acme.com",
                    "location": "Bangalore",
                    "industry": "Tech"
                }
                """;

        mockMvc.perform(post("/api/employer-profiles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Employer profile already exists for this user"));
    }

    @Test
    @DisplayName("GET /api/employer-profiles/me returns 200 OK with authenticated employer's profile")
    void getMyProfile_Returns200() throws Exception {
        when(employerProfileService.getMyProfile()).thenReturn(sampleProfile);

        mockMvc.perform(get("/api/employer-profiles/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.userId").value("employerA_id"))
                .andExpect(jsonPath("$.data.companyName").value("Acme Corp"));
    }

    @Test
    @DisplayName("GET /api/employer-profiles/{userId} returns 200 OK for owner")
    void getProfile_ByOwner_Returns200() throws Exception {
        when(employerProfileService.getProfileByUserId("employerA_id")).thenReturn(sampleProfile);

        mockMvc.perform(get("/api/employer-profiles/employerA_id"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.userId").value("employerA_id"));
    }

    @Test
    @DisplayName("GET /api/employer-profiles/{userId} returns 403 Forbidden when unauthorized")
    void getProfile_Unauthorized_Returns403() throws Exception {
        when(employerProfileService.getProfileByUserId("employerA_id"))
                .thenThrow(new AccessDeniedException("You are not authorized to view this profile"));

        mockMvc.perform(get("/api/employer-profiles/employerA_id"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("You are not authorized to view this profile"));
    }

    @Test
    @DisplayName("GET /api/employer-profiles/{userId} returns 404 Not Found when profile does not exist")
    void getProfile_NotFound_Returns404() throws Exception {
        when(employerProfileService.getProfileByUserId("non_existent"))
                .thenThrow(new ResourceNotFoundException("Employer profile not found for user id: non_existent"));

        mockMvc.perform(get("/api/employer-profiles/non_existent"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Employer profile not found for user id: non_existent"));
    }

    @Test
    @DisplayName("PUT /api/employer-profiles/{userId} returns 403 Forbidden when another employer tries to update")
    void updateProfile_Unauthorized_Returns403() throws Exception {
        when(employerProfileService.updateProfile(eq("employerA_id"), any(EmployerProfileRequest.class)))
                .thenThrow(new AccessDeniedException("You are not authorized to modify this profile"));

        String jsonBody = """
                {
                    "companyName": "Acme Updated",
                    "companyDescription": "Updated description",
                    "location": "Bangalore"
                }
                """;

        mockMvc.perform(put("/api/employer-profiles/employerA_id")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("You are not authorized to modify this profile"));
    }

    @Test
    @DisplayName("DELETE /api/employer-profiles/{userId} returns 200 OK when owner deletes profile")
    void deleteProfile_ByOwner_Returns200() throws Exception {
        doNothing().when(employerProfileService).deleteProfile("employerA_id");

        mockMvc.perform(delete("/api/employer-profiles/employerA_id"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Employer profile deleted successfully"));

        verify(employerProfileService, times(1)).deleteProfile("employerA_id");
    }

    @Test
    @DisplayName("DELETE /api/employer-profiles/{userId} returns 403 Forbidden when unauthorized user tries to delete")
    void deleteProfile_Unauthorized_Returns403() throws Exception {
        doThrow(new AccessDeniedException("You are not authorized to delete this profile"))
                .when(employerProfileService).deleteProfile("employerA_id");

        mockMvc.perform(delete("/api/employer-profiles/employerA_id"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("You are not authorized to delete this profile"));
    }
}
