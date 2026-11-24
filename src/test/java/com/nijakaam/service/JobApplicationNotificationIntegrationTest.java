package com.nijakaam.service;

import com.nijakaam.entity.Job;
import com.nijakaam.entity.JobApplication;
import com.nijakaam.entity.User;
import com.nijakaam.enums.ApplicationStatus;
import com.nijakaam.enums.JobStatus;
import com.nijakaam.enums.NotificationType;
import com.nijakaam.enums.Role;
import com.nijakaam.repository.JobApplicationRepository;
import com.nijakaam.repository.JobRepository;
import com.nijakaam.repository.UserRepository;
import com.nijakaam.service.impl.JobApplicationServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
class JobApplicationNotificationIntegrationTest {

    @Mock
    private JobApplicationRepository jobApplicationRepository;

    @Mock
    private JobRepository jobRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private JobApplicationServiceImpl jobApplicationService;

    private User worker;
    private User employer;
    private Job openJob;
    private JobApplication sampleApplication;

    @BeforeEach
    void setUp() {
        worker = new User("Worker Joe", "worker@nijakaam.com", "pass123", "9998887771", Role.WORKER);
        worker.setId("worker_id");

        employer = new User("Employer Corp", "employer@nijakaam.com", "pass123", "9998887772", Role.EMPLOYER);
        employer.setId("employer_id");

        openJob = new Job("Electrician Needed", "Description with more than twenty characters", employer.getId(), List.of("Wiring"), 5000.0, "Belagavi");
        openJob.setId("job_1");
        openJob.setStatus(JobStatus.OPEN);

        sampleApplication = new JobApplication(openJob.getId(), worker.getId(), employer.getId(), "I am qualified");
        sampleApplication.setId("app_1");
        sampleApplication.setStatus(ApplicationStatus.PENDING);
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
    @DisplayName("Test 1: Worker applies and employer receives notification (NEW_JOB_APPLICATION)")
    void workerApplies_EmployerReceivesNotification() {
        authenticateUser(worker);

        when(jobRepository.findById("job_1")).thenReturn(Optional.of(openJob));
        when(jobApplicationRepository.existsByWorkerIdAndJobId(worker.getId(), openJob.getId())).thenReturn(false);
        when(userRepository.findById(employer.getId())).thenReturn(Optional.of(employer));
        when(jobApplicationRepository.save(any(JobApplication.class))).thenAnswer(invocation -> invocation.getArgument(0));

        JobApplication request = new JobApplication();
        request.setJobId("job_1");
        request.setApplicationMessage("Interested in this role");

        JobApplication created = jobApplicationService.applyForJob(request);

        assertNotNull(created);
        verify(notificationService, times(1)).sendNotification(
                eq(employer.getId()),
                eq("New Job Application"),
                contains("Electrician Needed"),
                eq(NotificationType.NEW_JOB_APPLICATION),
                any()
        );
    }

    @Test
    @DisplayName("Test 2: Employer accepts application and worker receives notification (APPLICATION_ACCEPTED)")
    void employerAccepts_WorkerReceivesNotification() {
        authenticateUser(employer);

        when(jobApplicationRepository.findById("app_1")).thenReturn(Optional.of(sampleApplication));
        when(jobApplicationRepository.save(any(JobApplication.class))).thenAnswer(invocation -> invocation.getArgument(0));

        JobApplication updated = jobApplicationService.updateApplicationStatus("app_1", ApplicationStatus.ACCEPTED);

        assertNotNull(updated);
        assertEquals(ApplicationStatus.ACCEPTED, updated.getStatus());
        verify(notificationService, times(1)).sendNotification(
                eq(worker.getId()),
                eq("Application Accepted"),
                contains("accepted"),
                eq(NotificationType.APPLICATION_ACCEPTED),
                eq("app_1")
        );
    }

    @Test
    @DisplayName("Test 3: Employer rejects application and worker receives notification (APPLICATION_REJECTED)")
    void employerRejects_WorkerReceivesNotification() {
        authenticateUser(employer);

        when(jobApplicationRepository.findById("app_1")).thenReturn(Optional.of(sampleApplication));
        when(jobApplicationRepository.save(any(JobApplication.class))).thenAnswer(invocation -> invocation.getArgument(0));

        JobApplication updated = jobApplicationService.updateApplicationStatus("app_1", ApplicationStatus.REJECTED);

        assertNotNull(updated);
        assertEquals(ApplicationStatus.REJECTED, updated.getStatus());
        verify(notificationService, times(1)).sendNotification(
                eq(worker.getId()),
                eq("Application Rejected"),
                contains("rejected"),
                eq(NotificationType.APPLICATION_REJECTED),
                eq("app_1")
        );
    }

    @Test
    @DisplayName("Test 4: Worker withdraws application and employer receives notification (APPLICATION_WITHDRAWN)")
    void workerWithdraws_EmployerReceivesNotification() {
        authenticateUser(worker);

        when(jobApplicationRepository.findById("app_1")).thenReturn(Optional.of(sampleApplication));
        when(jobApplicationRepository.save(any(JobApplication.class))).thenAnswer(invocation -> invocation.getArgument(0));

        jobApplicationService.withdrawApplication("app_1");

        verify(notificationService, times(1)).sendNotification(
                eq(employer.getId()),
                eq("Application Withdrawn"),
                contains("withdrawn"),
                eq(NotificationType.APPLICATION_WITHDRAWN),
                eq("app_1")
        );
    }

    @Test
    @DisplayName("Test 11: Failed application operations do not generate notifications")
    void failedApplicationOperation_DoesNotGenerateNotification() {
        authenticateUser(worker);

        when(jobRepository.findById("job_1")).thenReturn(Optional.of(openJob));
        // Simulate already applied
        when(jobApplicationRepository.existsByWorkerIdAndJobId(worker.getId(), openJob.getId())).thenReturn(true);

        JobApplication request = new JobApplication();
        request.setJobId("job_1");

        assertThrows(ResponseStatusException.class, () -> jobApplicationService.applyForJob(request));

        // Zero notifications sent on failure
        verify(notificationService, never()).sendNotification(any(), any(), any(), any(), any());
    }
}
