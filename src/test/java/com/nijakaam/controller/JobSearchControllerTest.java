package com.nijakaam.controller;

import com.nijakaam.dto.request.JobSearchCriteria;
import com.nijakaam.dto.response.JobResponse;
import com.nijakaam.dto.response.PagedResponse;
import com.nijakaam.entity.Job;
import com.nijakaam.enums.JobStatus;
import com.nijakaam.exception.GlobalExceptionHandler;
import com.nijakaam.service.JobService;
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

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class JobSearchControllerTest {

    private MockMvc mockMvc;

    @Mock
    private JobService jobService;

    @InjectMocks
    private JobController jobController;

    private JobResponse sampleJobResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(jobController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        Job sampleJob = new Job(
                "Electrician Needed",
                "Expert wiring work for commercial space",
                "emp_123",
                List.of("Wiring", "Electrical"),
                10000.0,
                "Belagavi"
        );
        sampleJob.setId("job_1");
        sampleJob.setStatus(JobStatus.OPEN);

        sampleJobResponse = JobResponse.fromEntity(sampleJob);
    }

    @Test
    @DisplayName("GET /api/jobs/search returns 200 OK with paginated jobs")
    void searchJobs_Success_Returns200() throws Exception {
        PagedResponse<JobResponse> pagedResponse = new PagedResponse<>(
                List.of(sampleJobResponse),
                0,
                10,
                1L,
                1,
                true
        );

        when(jobService.searchJobs(any(JobSearchCriteria.class))).thenReturn(pagedResponse);

        mockMvc.perform(get("/api/jobs/search")
                        .param("keyword", "Electrician")
                        .param("skill", "Wiring")
                        .param("location", "Belagavi")
                        .param("minBudget", "5000")
                        .param("maxBudget", "20000")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Jobs retrieved successfully"))
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].id").value("job_1"))
                .andExpect(jsonPath("$.data.content[0].title").value("Electrician Needed"))
                .andExpect(jsonPath("$.data.content[0].skillsRequired[0]").value("Wiring"))
                .andExpect(jsonPath("$.data.content[0].budget").value(10000.0))
                .andExpect(jsonPath("$.data.content[0].status").value("OPEN"))
                .andExpect(jsonPath("$.data.content[0].location").value("Belagavi"))
                .andExpect(jsonPath("$.data.page").value(0))
                .andExpect(jsonPath("$.data.size").value(10))
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.totalPages").value(1))
                .andExpect(jsonPath("$.data.last").value(true));
    }

    @Test
    @DisplayName("GET /api/jobs/search returns 400 Bad Request on invalid budget parameters")
    void searchJobs_ValidationFailure_Returns400() throws Exception {
        when(jobService.searchJobs(any(JobSearchCriteria.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Minimum budget cannot be greater than maximum budget"));

        mockMvc.perform(get("/api/jobs/search")
                        .param("minBudget", "20000")
                        .param("maxBudget", "5000")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Minimum budget cannot be greater than maximum budget"));
    }

    @Test
    @DisplayName("GET /api/jobs/search returns 403 Forbidden when unauthenticated")
    void searchJobs_Unauthenticated_Returns403() throws Exception {
        when(jobService.searchJobs(any(JobSearchCriteria.class)))
                .thenThrow(new AccessDeniedException("User is not authenticated"));

        mockMvc.perform(get("/api/jobs/search")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("User is not authenticated"));
    }
}
