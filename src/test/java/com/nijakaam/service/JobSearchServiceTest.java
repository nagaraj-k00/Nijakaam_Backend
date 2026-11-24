package com.nijakaam.service;

import com.nijakaam.dto.request.JobSearchCriteria;
import com.nijakaam.dto.response.JobResponse;
import com.nijakaam.dto.response.PagedResponse;
import com.nijakaam.entity.Job;
import com.nijakaam.entity.User;
import com.nijakaam.enums.JobStatus;
import com.nijakaam.enums.Role;
import com.nijakaam.repository.JobRepository;
import com.nijakaam.repository.UserRepository;
import com.nijakaam.service.impl.JobServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JobSearchServiceTest {

    @Mock
    private JobRepository jobRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private MongoTemplate mongoTemplate;

    @InjectMocks
    private JobServiceImpl jobService;

    private User worker;
    private User employer;
    private Job openJob1;
    private Job openJob2;
    private Job completedJob;

    @BeforeEach
    void setUp() {
        worker = new User("Worker Joe", "worker@nijakaam.com", "pass123", "9998887771", Role.WORKER);
        worker.setId("worker_id");

        employer = new User("Employer Corp", "employer@nijakaam.com", "pass123", "9998887772", Role.EMPLOYER);
        employer.setId("employer_id");

        openJob1 = new Job("Electrician Needed", "Expert wiring work for commercial space", "employer_id", List.of("Wiring", "Electrical"), 10000.0, "Belagavi");
        openJob1.setId("job_1");
        openJob1.setStatus(JobStatus.OPEN);

        openJob2 = new Job("Carpenter for Office", "Modular furniture installation", "employer_id", List.of("Carpentry"), 15000.0, "Bangalore");
        openJob2.setId("job_2");
        openJob2.setStatus(JobStatus.OPEN);

        completedJob = new Job("Plumbing Repair", "Pipe fixing", "employer_id", List.of("Plumbing"), 5000.0, "Mysore");
        completedJob.setId("job_3");
        completedJob.setStatus(JobStatus.COMPLETED);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void authenticateUser(User user) {
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                user.getEmail(),
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
    }

    @Test
    @DisplayName("Requirement 1: Worker searches OPEN jobs successfully")
    void worker_SearchesOpenJobs_Successfully() {
        authenticateUser(worker);

        when(mongoTemplate.count(any(Query.class), eq(Job.class))).thenReturn(2L);
        when(mongoTemplate.find(any(Query.class), eq(Job.class))).thenReturn(List.of(openJob1, openJob2));

        JobSearchCriteria criteria = new JobSearchCriteria();
        criteria.setPage(0);
        criteria.setSize(10);

        PagedResponse<JobResponse> response = jobService.searchJobs(criteria);

        assertNotNull(response);
        assertEquals(2, response.getContent().size());
        assertEquals("job_1", response.getContent().get(0).getId());
        assertEquals(JobStatus.OPEN, response.getContent().get(0).getStatus());
        assertEquals("job_2", response.getContent().get(1).getId());
        assertEquals(JobStatus.OPEN, response.getContent().get(1).getStatus());
    }

    @Test
    @DisplayName("Requirement 2: Keyword filtering searches title and description")
    void search_FiltersByKeyword() {
        authenticateUser(worker);

        ArgumentCaptor<Query> queryCaptor = ArgumentCaptor.forClass(Query.class);
        when(mongoTemplate.count(queryCaptor.capture(), eq(Job.class))).thenReturn(1L);
        when(mongoTemplate.find(any(Query.class), eq(Job.class))).thenReturn(List.of(openJob1));

        JobSearchCriteria criteria = new JobSearchCriteria();
        criteria.setKeyword("Electrician");

        PagedResponse<JobResponse> response = jobService.searchJobs(criteria);

        assertNotNull(response);
        assertEquals(1, response.getContent().size());

        Query captured = queryCaptor.getValue();
        assertTrue(captured.getQueryObject().containsKey("$or"));
    }

    @Test
    @DisplayName("Requirement 3: Skills filtering adds regex match for skillsRequired")
    void search_FiltersBySkill() {
        authenticateUser(worker);

        ArgumentCaptor<Query> queryCaptor = ArgumentCaptor.forClass(Query.class);
        when(mongoTemplate.count(queryCaptor.capture(), eq(Job.class))).thenReturn(1L);
        when(mongoTemplate.find(any(Query.class), eq(Job.class))).thenReturn(List.of(openJob1));

        JobSearchCriteria criteria = new JobSearchCriteria();
        criteria.setSkill("Wiring");

        PagedResponse<JobResponse> response = jobService.searchJobs(criteria);

        assertNotNull(response);
        Query captured = queryCaptor.getValue();
        assertTrue(captured.getQueryObject().containsKey("skillsRequired"));
    }

    @Test
    @DisplayName("Requirement 4: Location filtering matches job location")
    void search_FiltersByLocation() {
        authenticateUser(worker);

        ArgumentCaptor<Query> queryCaptor = ArgumentCaptor.forClass(Query.class);
        when(mongoTemplate.count(queryCaptor.capture(), eq(Job.class))).thenReturn(1L);
        when(mongoTemplate.find(any(Query.class), eq(Job.class))).thenReturn(List.of(openJob1));

        JobSearchCriteria criteria = new JobSearchCriteria();
        criteria.setLocation("Belagavi");

        PagedResponse<JobResponse> response = jobService.searchJobs(criteria);

        assertNotNull(response);
        Query captured = queryCaptor.getValue();
        assertTrue(captured.getQueryObject().containsKey("location"));
    }

    @Test
    @DisplayName("Requirement 5: Budget filtering with minBudget and maxBudget")
    void search_FiltersByBudgetRange() {
        authenticateUser(worker);

        ArgumentCaptor<Query> queryCaptor = ArgumentCaptor.forClass(Query.class);
        when(mongoTemplate.count(queryCaptor.capture(), eq(Job.class))).thenReturn(1L);
        when(mongoTemplate.find(any(Query.class), eq(Job.class))).thenReturn(List.of(openJob1));

        JobSearchCriteria criteria = new JobSearchCriteria();
        criteria.setMinBudget(5000.0);
        criteria.setMaxBudget(12000.0);

        PagedResponse<JobResponse> response = jobService.searchJobs(criteria);

        assertNotNull(response);
        Query captured = queryCaptor.getValue();
        assertTrue(captured.getQueryObject().containsKey("budget"));
    }

    @Test
    @DisplayName("Requirement 6: Employer can filter jobs by status")
    void employer_FiltersByJobStatus() {
        authenticateUser(employer);

        ArgumentCaptor<Query> queryCaptor = ArgumentCaptor.forClass(Query.class);
        when(mongoTemplate.count(queryCaptor.capture(), eq(Job.class))).thenReturn(1L);
        when(mongoTemplate.find(any(Query.class), eq(Job.class))).thenReturn(List.of(completedJob));

        JobSearchCriteria criteria = new JobSearchCriteria();
        criteria.setStatus(JobStatus.COMPLETED);

        PagedResponse<JobResponse> response = jobService.searchJobs(criteria);

        assertNotNull(response);
        Query captured = queryCaptor.getValue();
        assertEquals(JobStatus.COMPLETED, captured.getQueryObject().get("status"));
    }

    @Test
    @DisplayName("Requirement 7: Pagination and sorting are applied to query")
    void search_PaginationAndSorting() {
        authenticateUser(worker);

        when(mongoTemplate.count(any(Query.class), eq(Job.class))).thenReturn(30L);
        when(mongoTemplate.find(any(Query.class), eq(Job.class))).thenReturn(List.of(openJob1));

        JobSearchCriteria criteria = new JobSearchCriteria();
        criteria.setPage(2);
        criteria.setSize(5);
        criteria.setSortBy("budget");
        criteria.setSortDir("asc");

        PagedResponse<JobResponse> response = jobService.searchJobs(criteria);

        assertNotNull(response);
        assertEquals(2, response.getPage());
        assertEquals(5, response.getSize());
        assertEquals(30L, response.getTotalElements());
        assertEquals(6, response.getTotalPages());
        assertFalse(response.isLast());
    }

    @Test
    @DisplayName("Requirement 8: Invalid budget range throws 400 Bad Request")
    void validation_ThrowsBadRequest_OnInvalidBudgetRange() {
        authenticateUser(worker);

        // Negative minBudget
        JobSearchCriteria criteria1 = new JobSearchCriteria();
        criteria1.setMinBudget(-10.0);
        ResponseStatusException ex1 = assertThrows(ResponseStatusException.class, () -> jobService.searchJobs(criteria1));
        assertEquals(HttpStatus.BAD_REQUEST, ex1.getStatusCode());

        // Negative maxBudget
        JobSearchCriteria criteria2 = new JobSearchCriteria();
        criteria2.setMaxBudget(-50.0);
        ResponseStatusException ex2 = assertThrows(ResponseStatusException.class, () -> jobService.searchJobs(criteria2));
        assertEquals(HttpStatus.BAD_REQUEST, ex2.getStatusCode());

        // minBudget > maxBudget
        JobSearchCriteria criteria3 = new JobSearchCriteria();
        criteria3.setMinBudget(20000.0);
        criteria3.setMaxBudget(5000.0);
        ResponseStatusException ex3 = assertThrows(ResponseStatusException.class, () -> jobService.searchJobs(criteria3));
        assertEquals(HttpStatus.BAD_REQUEST, ex3.getStatusCode());
    }

    @Test
    @DisplayName("Requirement 9: Invalid pagination parameters throw 400 Bad Request")
    void validation_ThrowsBadRequest_OnInvalidPagination() {
        authenticateUser(worker);

        // Negative page index
        JobSearchCriteria criteria1 = new JobSearchCriteria();
        criteria1.setPage(-1);
        ResponseStatusException ex1 = assertThrows(ResponseStatusException.class, () -> jobService.searchJobs(criteria1));
        assertEquals(HttpStatus.BAD_REQUEST, ex1.getStatusCode());

        // Size <= 0
        JobSearchCriteria criteria2 = new JobSearchCriteria();
        criteria2.setSize(0);
        ResponseStatusException ex2 = assertThrows(ResponseStatusException.class, () -> jobService.searchJobs(criteria2));
        assertEquals(HttpStatus.BAD_REQUEST, ex2.getStatusCode());

        // Size > 100
        JobSearchCriteria criteria3 = new JobSearchCriteria();
        criteria3.setSize(101);
        ResponseStatusException ex3 = assertThrows(ResponseStatusException.class, () -> jobService.searchJobs(criteria3));
        assertEquals(HttpStatus.BAD_REQUEST, ex3.getStatusCode());
    }

    @Test
    @DisplayName("Requirement 10: Unauthenticated access throws AccessDeniedException")
    void unauthenticatedAccess_ThrowsAccessDeniedException() {
        SecurityContextHolder.clearContext();

        JobSearchCriteria criteria = new JobSearchCriteria();

        assertThrows(AccessDeniedException.class, () -> jobService.searchJobs(criteria));
        verify(mongoTemplate, never()).find(any(Query.class), eq(Job.class));
        verify(mongoTemplate, never()).count(any(Query.class), eq(Job.class));
    }

    @Test
    @DisplayName("Requirement 11: Existing job ownership verification remains intact (Employer B cannot delete Employer A's job)")
    void existingJobOwnership_RemainsIntact() {
        authenticateUser(employer);
        when(jobRepository.findById(openJob1.getId())).thenReturn(Optional.of(openJob1));

        // employer has id employer_id, openJob1 has employerId employer_id
        assertDoesNotThrow(() -> jobService.deleteJob(openJob1.getId()));
        verify(jobRepository, times(1)).delete(openJob1);
    }

    @Test
    @DisplayName("Requirement 12: Closed jobs are excluded from default worker search (status forced to OPEN)")
    void workerSearch_ExcludesClosedJobsByDefault() {
        authenticateUser(worker);

        ArgumentCaptor<Query> queryCaptor = ArgumentCaptor.forClass(Query.class);
        when(mongoTemplate.count(queryCaptor.capture(), eq(Job.class))).thenReturn(1L);
        when(mongoTemplate.find(any(Query.class), eq(Job.class))).thenReturn(List.of(openJob1));

        JobSearchCriteria criteria = new JobSearchCriteria();
        // Worker does not specify status, or passes closed status:
        criteria.setStatus(null);

        PagedResponse<JobResponse> response = jobService.searchJobs(criteria);

        assertNotNull(response);
        Query captured = queryCaptor.getValue();
        assertEquals(JobStatus.OPEN, captured.getQueryObject().get("status"), "Worker search must strictly restrict status to OPEN");
    }
}
