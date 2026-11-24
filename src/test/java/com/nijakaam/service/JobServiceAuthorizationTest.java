package com.nijakaam.service;

import com.nijakaam.entity.Job;
import com.nijakaam.entity.User;
import com.nijakaam.enums.JobStatus;
import com.nijakaam.enums.Role;
import com.nijakaam.exception.ResourceNotFoundException;
import com.nijakaam.repository.JobRepository;
import com.nijakaam.repository.UserRepository;
import com.nijakaam.service.impl.JobServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JobServiceAuthorizationTest {

    @Mock
    private JobRepository jobRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private JobServiceImpl jobService;

    private User employerA;
    private User employerB;
    private User worker;
    private Job jobA;

    @BeforeEach
    void setUp() {
        employerA = new User("Employer A", "employerA@nijakaam.com", "pass123", "111", Role.EMPLOYER);
        employerA.setId("empA_id");

        employerB = new User("Employer B", "employerB@nijakaam.com", "pass123", "222", Role.EMPLOYER);
        employerB.setId("empB_id");

        worker = new User("Worker User", "worker@nijakaam.com", "pass123", "333", Role.WORKER);
        worker.setId("worker_id");

        jobA = new Job("Plumber Needed", "Fix plumbing issues", employerA.getId(), List.of("Plumbing"), 500.0, "Bangalore");
        jobA.setId("jobA_id");
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
    @DisplayName("Employer A can successfully delete their own job")
    void employerA_CanDeleteTheirOwnJob() {
        authenticateUser(employerA);
        when(jobRepository.findById(jobA.getId())).thenReturn(Optional.of(jobA));

        assertDoesNotThrow(() -> jobService.deleteJob(jobA.getId()));

        verify(jobRepository, times(1)).delete(jobA);
    }

    @Test
    @DisplayName("Employer B CANNOT delete Employer A's job -> throws AccessDeniedException")
    void employerB_CannotDeleteEmployerAJob() {
        authenticateUser(employerB);
        when(jobRepository.findById(jobA.getId())).thenReturn(Optional.of(jobA));

        AccessDeniedException exception = assertThrows(
                AccessDeniedException.class,
                () -> jobService.deleteJob(jobA.getId())
        );

        assertEquals("You are not authorized to modify this job", exception.getMessage());
        verify(jobRepository, never()).delete(any(Job.class));
    }

    @Test
    @DisplayName("Employer A can successfully update their own job")
    void employerA_CanUpdateTheirOwnJob() {
        authenticateUser(employerA);
        when(jobRepository.findById(jobA.getId())).thenReturn(Optional.of(jobA));
        when(jobRepository.save(any(Job.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Job updatedDetails = new Job("Updated Title", "Updated Description", "empA_id", List.of("Plumbing", "Pipe Fitting"), 600.0, "Mysore");

        Job result = jobService.updateJob(jobA.getId(), updatedDetails);

        assertNotNull(result);
        assertEquals("Updated Title", result.getTitle());
        assertEquals("Updated Description", result.getDescription());
        assertEquals(600.0, result.getBudget());
        assertEquals("Mysore", result.getLocation());
        assertEquals("empA_id", result.getEmployerId()); // Ownership preserved
        verify(jobRepository, times(1)).save(jobA);
    }

    @Test
    @DisplayName("Employer B CANNOT update Employer A's job -> throws AccessDeniedException")
    void employerB_CannotUpdateEmployerAJob() {
        authenticateUser(employerB);
        when(jobRepository.findById(jobA.getId())).thenReturn(Optional.of(jobA));

        Job updatedDetails = new Job("Malicious Update", "Description", "empB_id", List.of("Plumbing"), 100.0, "Nowhere");

        AccessDeniedException exception = assertThrows(
                AccessDeniedException.class,
                () -> jobService.updateJob(jobA.getId(), updatedDetails)
        );

        assertEquals("You are not authorized to modify this job", exception.getMessage());
        verify(jobRepository, never()).save(any(Job.class));
    }

    @Test
    @DisplayName("Employer A can successfully update their own job status")
    void employerA_CanUpdateTheirOwnJobStatus() {
        authenticateUser(employerA);
        when(jobRepository.findById(jobA.getId())).thenReturn(Optional.of(jobA));
        when(jobRepository.save(any(Job.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Job result = jobService.updateJobStatus(jobA.getId(), JobStatus.COMPLETED);

        assertNotNull(result);
        assertEquals(JobStatus.COMPLETED, result.getStatus());
        verify(jobRepository, times(1)).save(jobA);
    }

    @Test
    @DisplayName("Employer B CANNOT update status of Employer A's job -> throws AccessDeniedException")
    void employerB_CannotUpdateEmployerAJobStatus() {
        authenticateUser(employerB);
        when(jobRepository.findById(jobA.getId())).thenReturn(Optional.of(jobA));

        AccessDeniedException exception = assertThrows(
                AccessDeniedException.class,
                () -> jobService.updateJobStatus(jobA.getId(), JobStatus.CANCELLED)
        );

        assertEquals("You are not authorized to modify this job", exception.getMessage());
        verify(jobRepository, never()).save(any(Job.class));
    }

    @Test
    @DisplayName("Worker role cannot delete any job -> throws AccessDeniedException")
    void worker_CannotDeleteJob() {
        authenticateUser(worker);

        AccessDeniedException exception = assertThrows(
                AccessDeniedException.class,
                () -> jobService.deleteJob(jobA.getId())
        );

        assertEquals("Only employers can perform this operation", exception.getMessage());
        verify(jobRepository, never()).delete(any(Job.class));
    }

    @Test
    @DisplayName("Worker role cannot update any job -> throws AccessDeniedException")
    void worker_CannotUpdateJob() {
        authenticateUser(worker);

        Job updatedDetails = new Job("Worker Edit", "Description", "worker_id", List.of(), 10.0, "Here");

        AccessDeniedException exception = assertThrows(
                AccessDeniedException.class,
                () -> jobService.updateJob(jobA.getId(), updatedDetails)
        );

        assertEquals("Only employers can perform this operation", exception.getMessage());
        verify(jobRepository, never()).save(any(Job.class));
    }

    @Test
    @DisplayName("Deleting non-existent job throws ResourceNotFoundException (HTTP 404)")
    void deleteJob_WhenJobDoesNotExist_ThrowsResourceNotFoundException() {
        authenticateUser(employerA);
        when(jobRepository.findById("non_existent")).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> jobService.deleteJob("non_existent")
        );

        assertTrue(exception.getMessage().contains("Job not found with id: non_existent"));
        verify(jobRepository, never()).delete(any(Job.class));
    }

    @Test
    @DisplayName("Updating non-existent job throws ResourceNotFoundException (HTTP 404)")
    void updateJob_WhenJobDoesNotExist_ThrowsResourceNotFoundException() {
        authenticateUser(employerA);
        when(jobRepository.findById("non_existent")).thenReturn(Optional.empty());

        Job updatedDetails = new Job("Title", "Description", "empA_id", List.of(), 100.0, "City");

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> jobService.updateJob("non_existent", updatedDetails)
        );

        assertTrue(exception.getMessage().contains("Job not found with id: non_existent"));
        verify(jobRepository, never()).save(any(Job.class));
    }

    @Test
    @DisplayName("createJob overrides any client-supplied employerId with authenticated employer's database ID")
    void createJob_SetsAuthenticatedEmployerId() {
        authenticateUser(employerA);
        when(jobRepository.save(any(Job.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Client attempts to pass Employer B's ID in body
        Job spoofedJob = new Job("New Job", "Description", "spoofed_empB_id", List.of("Skill"), 300.0, "City");

        Job created = jobService.createJob(spoofedJob);

        assertNotNull(created);
        assertEquals(employerA.getId(), created.getEmployerId(), "Employer ID must be overridden by authenticated employer's ID");
        assertEquals(JobStatus.OPEN, created.getStatus());
        verify(jobRepository, times(1)).save(any(Job.class));
    }
}
