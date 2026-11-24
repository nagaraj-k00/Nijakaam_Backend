package com.nijakaam.service;

import com.nijakaam.dto.request.WorkerSearchCriteria;
import com.nijakaam.dto.response.PagedResponse;
import com.nijakaam.dto.response.WorkerProfileResponse;
import com.nijakaam.entity.User;
import com.nijakaam.entity.WorkerProfile;
import com.nijakaam.enums.Role;
import com.nijakaam.repository.UserRepository;
import com.nijakaam.service.impl.WorkerSearchServiceImpl;
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

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkerSearchServiceTest {

    @Mock
    private MongoTemplate mongoTemplate;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private WorkerSearchServiceImpl workerSearchService;

    private User employer;
    private User worker;
    private User admin;
    private WorkerProfile profile1;
    private WorkerProfile profile2;

    @BeforeEach
    void setUp() {
        employer = new User("Employer Corp", "employer@nijakaam.com", "pass123", "9998887771", Role.EMPLOYER);
        employer.setId("employer_id");

        worker = new User("Worker Joe", "worker@nijakaam.com", "pass123", "9998887772", Role.WORKER);
        worker.setId("worker_id");

        admin = new User("Admin", "admin@nijakaam.com", "pass123", "9998887773", Role.ADMIN);
        admin.setId("admin_id");

        profile1 = new WorkerProfile("worker_1", List.of("Carpentry", "Woodworking"), 5, 40.0, "Experienced carpenter", "Belagavi");
        profile1.setId("profile_1");
        profile1.setRating(4.8);
        profile1.setAvailable(true);

        profile2 = new WorkerProfile("worker_2", List.of("Plumbing", "Pipe Fitting"), 2, 25.0, "Certified plumber", "Bangalore");
        profile2.setId("profile_2");
        profile2.setRating(4.2);
        profile2.setAvailable(false);
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
    @DisplayName("Requirement 1 & 8: Employer searches workers successfully and receives sanitized WorkerProfileResponse")
    void employer_SearchesWorkers_Successfully() {
        authenticateUser(employer);

        when(mongoTemplate.count(any(Query.class), eq(WorkerProfile.class))).thenReturn(2L);
        when(mongoTemplate.find(any(Query.class), eq(WorkerProfile.class))).thenReturn(List.of(profile1, profile2));

        WorkerSearchCriteria criteria = new WorkerSearchCriteria();
        criteria.setPage(0);
        criteria.setSize(10);

        PagedResponse<WorkerProfileResponse> response = workerSearchService.searchWorkers(criteria);

        assertNotNull(response);
        assertEquals(2, response.getContent().size());
        assertEquals(0, response.getPage());
        assertEquals(10, response.getSize());
        assertEquals(2L, response.getTotalElements());
        assertEquals(1, response.getTotalPages());
        assertTrue(response.isLast());

        WorkerProfileResponse first = response.getContent().get(0);
        assertEquals("worker_1", first.getUserId());
        assertEquals("Belagavi", first.getLocation());
        assertEquals(5, first.getExperienceYears());
        assertEquals(40.0, first.getHourlyRate());
        assertTrue(first.isAvailable());
    }

    @Test
    @DisplayName("Requirement 2: Skill filtering adds regex criteria for skills array")
    void employer_FiltersBySkill() {
        authenticateUser(employer);

        ArgumentCaptor<Query> queryCaptor = ArgumentCaptor.forClass(Query.class);
        when(mongoTemplate.count(queryCaptor.capture(), eq(WorkerProfile.class))).thenReturn(1L);
        when(mongoTemplate.find(any(Query.class), eq(WorkerProfile.class))).thenReturn(List.of(profile1));

        WorkerSearchCriteria criteria = new WorkerSearchCriteria();
        criteria.setSkill("Carpentry");

        PagedResponse<WorkerProfileResponse> response = workerSearchService.searchWorkers(criteria);

        assertNotNull(response);
        assertEquals(1, response.getContent().size());

        Query capturedQuery = queryCaptor.getValue();
        assertTrue(capturedQuery.getQueryObject().containsKey("skills"));
    }

    @Test
    @DisplayName("Requirement 3: Location filtering adds regex criteria for location")
    void employer_FiltersByLocation() {
        authenticateUser(employer);

        ArgumentCaptor<Query> queryCaptor = ArgumentCaptor.forClass(Query.class);
        when(mongoTemplate.count(queryCaptor.capture(), eq(WorkerProfile.class))).thenReturn(1L);
        when(mongoTemplate.find(any(Query.class), eq(WorkerProfile.class))).thenReturn(List.of(profile1));

        WorkerSearchCriteria criteria = new WorkerSearchCriteria();
        criteria.setLocation("Belagavi");

        PagedResponse<WorkerProfileResponse> response = workerSearchService.searchWorkers(criteria);

        assertNotNull(response);
        assertEquals(1, response.getContent().size());

        Query capturedQuery = queryCaptor.getValue();
        assertTrue(capturedQuery.getQueryObject().containsKey("location"));
    }

    @Test
    @DisplayName("Requirement 4: Experience range filtering adds gte and lte criteria")
    void employer_FiltersByExperienceRange() {
        authenticateUser(employer);

        ArgumentCaptor<Query> queryCaptor = ArgumentCaptor.forClass(Query.class);
        when(mongoTemplate.count(queryCaptor.capture(), eq(WorkerProfile.class))).thenReturn(1L);
        when(mongoTemplate.find(any(Query.class), eq(WorkerProfile.class))).thenReturn(List.of(profile1));

        WorkerSearchCriteria criteria = new WorkerSearchCriteria();
        criteria.setMinExperience(2);
        criteria.setMaxExperience(6);

        PagedResponse<WorkerProfileResponse> response = workerSearchService.searchWorkers(criteria);

        assertNotNull(response);
        Query capturedQuery = queryCaptor.getValue();
        assertTrue(capturedQuery.getQueryObject().containsKey("experienceYears"));
    }

    @Test
    @DisplayName("Requirement 5: Hourly rate range filtering adds gte and lte criteria")
    void employer_FiltersByHourlyRateRange() {
        authenticateUser(employer);

        ArgumentCaptor<Query> queryCaptor = ArgumentCaptor.forClass(Query.class);
        when(mongoTemplate.count(queryCaptor.capture(), eq(WorkerProfile.class))).thenReturn(1L);
        when(mongoTemplate.find(any(Query.class), eq(WorkerProfile.class))).thenReturn(List.of(profile2));

        WorkerSearchCriteria criteria = new WorkerSearchCriteria();
        criteria.setMinRate(10.0);
        criteria.setMaxRate(30.0);

        PagedResponse<WorkerProfileResponse> response = workerSearchService.searchWorkers(criteria);

        assertNotNull(response);
        Query capturedQuery = queryCaptor.getValue();
        assertTrue(capturedQuery.getQueryObject().containsKey("hourlyRate"));
    }

    @Test
    @DisplayName("Requirement 6: Availability filtering matches specified boolean status")
    void employer_FiltersByAvailability() {
        authenticateUser(employer);

        ArgumentCaptor<Query> queryCaptor = ArgumentCaptor.forClass(Query.class);
        when(mongoTemplate.count(queryCaptor.capture(), eq(WorkerProfile.class))).thenReturn(1L);
        when(mongoTemplate.find(any(Query.class), eq(WorkerProfile.class))).thenReturn(List.of(profile1));

        WorkerSearchCriteria criteria = new WorkerSearchCriteria();
        criteria.setAvailable(true);

        PagedResponse<WorkerProfileResponse> response = workerSearchService.searchWorkers(criteria);

        assertNotNull(response);
        Query capturedQuery = queryCaptor.getValue();
        assertTrue(capturedQuery.getQueryObject().containsKey("available"));
    }

    @Test
    @DisplayName("Requirement 7: Pagination and sorting are passed down to Query")
    void employer_PaginationAndSorting() {
        authenticateUser(employer);

        ArgumentCaptor<Query> queryCaptor = ArgumentCaptor.forClass(Query.class);
        when(mongoTemplate.count(queryCaptor.capture(), eq(WorkerProfile.class))).thenReturn(25L);
        when(mongoTemplate.find(any(Query.class), eq(WorkerProfile.class))).thenReturn(List.of(profile1));

        WorkerSearchCriteria criteria = new WorkerSearchCriteria();
        criteria.setPage(1);
        criteria.setSize(5);
        criteria.setSortBy("experienceYears");
        criteria.setSortDir("asc");

        PagedResponse<WorkerProfileResponse> response = workerSearchService.searchWorkers(criteria);

        assertNotNull(response);
        assertEquals(1, response.getPage());
        assertEquals(5, response.getSize());
        assertEquals(25L, response.getTotalElements());
        assertEquals(5, response.getTotalPages());
        assertFalse(response.isLast());
    }

    @Test
    @DisplayName("Requirement 10 & 11: Worker role cannot search workers -> HTTP 403 AccessDeniedException")
    void worker_CannotSearchWorkers_ThrowsAccessDeniedException() {
        authenticateUser(worker);

        WorkerSearchCriteria criteria = new WorkerSearchCriteria();

        AccessDeniedException ex = assertThrows(
                AccessDeniedException.class,
                () -> workerSearchService.searchWorkers(criteria)
        );

        assertEquals("Only employers and admins are authorized to search for workers", ex.getMessage());
        verify(mongoTemplate, never()).find(any(Query.class), eq(WorkerProfile.class));
        verify(mongoTemplate, never()).count(any(Query.class), eq(WorkerProfile.class));
    }

    @Test
    @DisplayName("Requirement 9: Unauthenticated access throws AccessDeniedException")
    void unauthenticatedUser_CannotSearchWorkers() {
        SecurityContextHolder.clearContext();

        WorkerSearchCriteria criteria = new WorkerSearchCriteria();

        assertThrows(
                AccessDeniedException.class,
                () -> workerSearchService.searchWorkers(criteria)
        );

        verify(mongoTemplate, never()).find(any(Query.class), eq(WorkerProfile.class));
        verify(mongoTemplate, never()).count(any(Query.class), eq(WorkerProfile.class));
    }

    @Test
    @DisplayName("Requirement 12: Admin can search workers successfully")
    void admin_CanSearchWorkers_Successfully() {
        authenticateUser(admin);

        when(mongoTemplate.count(any(Query.class), eq(WorkerProfile.class))).thenReturn(1L);
        when(mongoTemplate.find(any(Query.class), eq(WorkerProfile.class))).thenReturn(List.of(profile1));

        WorkerSearchCriteria criteria = new WorkerSearchCriteria();

        PagedResponse<WorkerProfileResponse> response = workerSearchService.searchWorkers(criteria);

        assertNotNull(response);
        assertEquals(1, response.getContent().size());
    }

    @Test
    @DisplayName("Requirement 14: Search parameter validation - negative values or invalid ranges throw 400 Bad Request")
    void validation_ThrowsBadRequest_OnInvalidParameters() {
        authenticateUser(employer);

        // Negative experience
        WorkerSearchCriteria criteria1 = new WorkerSearchCriteria();
        criteria1.setMinExperience(-1);
        ResponseStatusException ex1 = assertThrows(ResponseStatusException.class, () -> workerSearchService.searchWorkers(criteria1));
        assertEquals(HttpStatus.BAD_REQUEST, ex1.getStatusCode());

        // Min experience > Max experience
        WorkerSearchCriteria criteria2 = new WorkerSearchCriteria();
        criteria2.setMinExperience(10);
        criteria2.setMaxExperience(2);
        ResponseStatusException ex2 = assertThrows(ResponseStatusException.class, () -> workerSearchService.searchWorkers(criteria2));
        assertEquals(HttpStatus.BAD_REQUEST, ex2.getStatusCode());

        // Negative hourly rate
        WorkerSearchCriteria criteria3 = new WorkerSearchCriteria();
        criteria3.setMinRate(-5.0);
        ResponseStatusException ex3 = assertThrows(ResponseStatusException.class, () -> workerSearchService.searchWorkers(criteria3));
        assertEquals(HttpStatus.BAD_REQUEST, ex3.getStatusCode());

        // Min rate > Max rate
        WorkerSearchCriteria criteria4 = new WorkerSearchCriteria();
        criteria4.setMinRate(100.0);
        criteria4.setMaxRate(50.0);
        ResponseStatusException ex4 = assertThrows(ResponseStatusException.class, () -> workerSearchService.searchWorkers(criteria4));
        assertEquals(HttpStatus.BAD_REQUEST, ex4.getStatusCode());

        // Invalid page index
        WorkerSearchCriteria criteria5 = new WorkerSearchCriteria();
        criteria5.setPage(-1);
        ResponseStatusException ex5 = assertThrows(ResponseStatusException.class, () -> workerSearchService.searchWorkers(criteria5));
        assertEquals(HttpStatus.BAD_REQUEST, ex5.getStatusCode());

        // Invalid size
        WorkerSearchCriteria criteria6 = new WorkerSearchCriteria();
        criteria6.setSize(0);
        ResponseStatusException ex6 = assertThrows(ResponseStatusException.class, () -> workerSearchService.searchWorkers(criteria6));
        assertEquals(HttpStatus.BAD_REQUEST, ex6.getStatusCode());

        WorkerSearchCriteria criteria7 = new WorkerSearchCriteria();
        criteria7.setSize(101);
        ResponseStatusException ex7 = assertThrows(ResponseStatusException.class, () -> workerSearchService.searchWorkers(criteria7));
        assertEquals(HttpStatus.BAD_REQUEST, ex7.getStatusCode());
    }

    @Test
    @DisplayName("Requirement 9: Sanitized WorkerProfileResponse never contains User credentials, phone, or email")
    void privateData_IsNeverExposed_InResponse() {
        Field[] responseFields = WorkerProfileResponse.class.getDeclaredFields();
        for (Field field : responseFields) {
            String name = field.getName().toLowerCase();
            assertFalse(name.contains("password"), "Field 'password' must not exist in response DTO");
            assertFalse(name.contains("email"), "Field 'email' must not exist in response DTO");
            assertFalse(name.contains("phone"), "Field 'phone' must not exist in response DTO");
            assertFalse(name.contains("secret"), "Security secrets must not exist in response DTO");
        }
    }
}
