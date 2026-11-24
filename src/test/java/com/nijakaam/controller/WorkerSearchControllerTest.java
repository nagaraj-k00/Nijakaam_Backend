package com.nijakaam.controller;

import com.nijakaam.dto.request.WorkerSearchCriteria;
import com.nijakaam.dto.response.PagedResponse;
import com.nijakaam.dto.response.WorkerProfileResponse;
import com.nijakaam.entity.WorkerProfile;
import com.nijakaam.exception.GlobalExceptionHandler;
import com.nijakaam.service.WorkerSearchService;
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

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class WorkerSearchControllerTest {

    private MockMvc mockMvc;

    @Mock
    private WorkerSearchService workerSearchService;

    @InjectMocks
    private WorkerSearchController workerSearchController;

    private WorkerProfileResponse sampleWorker;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(workerSearchController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        WorkerProfile profile = new WorkerProfile(
                "worker_1",
                List.of("Carpentry", "Woodworking"),
                4,
                35.0,
                "Master carpenter",
                "Belagavi"
        );
        profile.setId("profile_1");
        profile.setRating(4.8);
        profile.setAvailable(true);

        sampleWorker = WorkerProfileResponse.fromEntity(profile);
    }

    @Test
    @DisplayName("GET /api/workers/search returns 200 OK with paginated list of workers for employer")
    void searchWorkers_Success_Returns200() throws Exception {
        PagedResponse<WorkerProfileResponse> pagedResponse = new PagedResponse<>(
                List.of(sampleWorker),
                0,
                10,
                1L,
                1,
                true
        );

        when(workerSearchService.searchWorkers(any(WorkerSearchCriteria.class))).thenReturn(pagedResponse);

        mockMvc.perform(get("/api/workers/search")
                        .param("skill", "Carpentry")
                        .param("location", "Belagavi")
                        .param("minExperience", "2")
                        .param("maxRate", "500")
                        .param("available", "true")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Workers retrieved successfully"))
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].userId").value("worker_1"))
                .andExpect(jsonPath("$.data.content[0].skills[0]").value("Carpentry"))
                .andExpect(jsonPath("$.data.content[0].location").value("Belagavi"))
                .andExpect(jsonPath("$.data.content[0].experienceYears").value(4))
                .andExpect(jsonPath("$.data.content[0].hourlyRate").value(35.0))
                .andExpect(jsonPath("$.data.content[0].available").value(true))
                .andExpect(jsonPath("$.data.page").value(0))
                .andExpect(jsonPath("$.data.size").value(10))
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.totalPages").value(1))
                .andExpect(jsonPath("$.data.last").value(true));
    }

    @Test
    @DisplayName("GET /api/workers/search returns 400 Bad Request when search parameters are invalid")
    void searchWorkers_ValidationFailure_Returns400() throws Exception {
        when(workerSearchService.searchWorkers(any(WorkerSearchCriteria.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Minimum experience cannot be greater than maximum experience"));

        mockMvc.perform(get("/api/workers/search")
                        .param("minExperience", "10")
                        .param("maxExperience", "2")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Minimum experience cannot be greater than maximum experience"));
    }

    @Test
    @DisplayName("GET /api/workers/search returns 403 Forbidden when unauthorized user/worker attempts to search")
    void searchWorkers_Unauthorized_Returns403() throws Exception {
        when(workerSearchService.searchWorkers(any(WorkerSearchCriteria.class)))
                .thenThrow(new AccessDeniedException("Only employers and admins are authorized to search for workers"));

        mockMvc.perform(get("/api/workers/search")
                        .param("skill", "Plumbing")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Only employers and admins are authorized to search for workers"));
    }

    @Test
    @DisplayName("GET /api/workers/search ensures response contains no sensitive fields")
    void searchWorkers_NeverExposesPrivateFields() throws Exception {
        PagedResponse<WorkerProfileResponse> pagedResponse = new PagedResponse<>(
                List.of(sampleWorker),
                0,
                10,
                1L,
                1,
                true
        );

        when(workerSearchService.searchWorkers(any(WorkerSearchCriteria.class))).thenReturn(pagedResponse);

        mockMvc.perform(get("/api/workers/search")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].password").doesNotExist())
                .andExpect(jsonPath("$.data.content[0].email").doesNotExist())
                .andExpect(jsonPath("$.data.content[0].phone").doesNotExist());
    }
}
