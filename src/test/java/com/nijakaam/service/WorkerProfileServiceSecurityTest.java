package com.nijakaam.service;

import com.nijakaam.dto.request.WorkerProfileRequest;
import com.nijakaam.entity.User;
import com.nijakaam.entity.WorkerProfile;
import com.nijakaam.enums.Role;
import com.nijakaam.exception.ResourceNotFoundException;
import com.nijakaam.repository.UserRepository;
import com.nijakaam.repository.WorkerProfileRepository;
import com.nijakaam.service.impl.WorkerProfileServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkerProfileServiceSecurityTest {

    @Mock
    private WorkerProfileRepository workerProfileRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private WorkerProfileServiceImpl workerProfileService;

    private User workerA;
    private User workerB;
    private User employer;
    private User admin;
    private WorkerProfile profileA;

    @BeforeEach
    void setUp() {
        workerA = new User("Worker A", "workerA@nijakaam.com", "pass123", "9998887771", Role.WORKER);
        workerA.setId("workerA_id");

        workerB = new User("Worker B", "workerB@nijakaam.com", "pass123", "9998887772", Role.WORKER);
        workerB.setId("workerB_id");

        employer = new User("Employer X", "employer@nijakaam.com", "pass123", "9998887773", Role.EMPLOYER);
        employer.setId("employer_id");

        admin = new User("Admin User", "admin@nijakaam.com", "pass123", "9998887774", Role.ADMIN);
        admin.setId("admin_id");

        profileA = new WorkerProfile("workerA_id", List.of("Plumbing", "Carpentry"), 4, 30.0, "Expert technician", "Bangalore");
        profileA.setId("profileA_id");
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
    @DisplayName("Scenario 1: Worker A creates their profile successfully")
    void workerA_CreatesProfile_Successfully() {
        authenticateUser(workerA);
        when(workerProfileRepository.existsByUserId(workerA.getId())).thenReturn(false);
        when(workerProfileRepository.save(any(WorkerProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        WorkerProfileRequest request = new WorkerProfileRequest(
                List.of("Plumbing", "Pipe Fitting"),
                3,
                25.0,
                "Certified plumber",
                "Bangalore",
                true
        );

        WorkerProfile created = workerProfileService.createProfile(request);

        assertNotNull(created);
        assertEquals(workerA.getId(), created.getUserId());
        assertEquals(3, created.getExperienceYears());
        assertEquals(25.0, created.getHourlyRate());
        assertEquals("Bangalore", created.getLocation());
        assertTrue(created.isAvailable());
        assertEquals(0.0, created.getRating());
        assertNotNull(created.getCreatedAt());
        assertNotNull(created.getUpdatedAt());
        verify(workerProfileRepository, times(1)).save(any(WorkerProfile.class));
    }

    @Test
    @DisplayName("Scenario 2: Worker A retrieves their own profile")
    void workerA_RetrievesOwnProfile_Successfully() {
        authenticateUser(workerA);
        when(workerProfileRepository.findByUserId(workerA.getId())).thenReturn(Optional.of(profileA));

        WorkerProfile retrieved = workerProfileService.getProfileByUserId(workerA.getId());

        assertNotNull(retrieved);
        assertEquals(profileA.getId(), retrieved.getId());
        assertEquals(workerA.getId(), retrieved.getUserId());
    }

    @Test
    @DisplayName("Worker A retrieves their own profile via getMyProfile() (/me endpoint)")
    void workerA_RetrievesOwnProfile_ViaGetMyProfile() {
        authenticateUser(workerA);
        when(workerProfileRepository.findByUserId(workerA.getId())).thenReturn(Optional.of(profileA));

        WorkerProfile retrieved = workerProfileService.getMyProfile();

        assertNotNull(retrieved);
        assertEquals(profileA.getId(), retrieved.getId());
        assertEquals(workerA.getId(), retrieved.getUserId());
    }

    @Test
    @DisplayName("Scenario 3: Worker A updates their own profile")
    void workerA_UpdatesOwnProfile_Successfully() {
        authenticateUser(workerA);
        when(workerProfileRepository.findByUserId(workerA.getId())).thenReturn(Optional.of(profileA));
        when(workerProfileRepository.save(any(WorkerProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        WorkerProfileRequest updateRequest = new WorkerProfileRequest(
                List.of("Plumbing", "Electrical"),
                5,
                35.0,
                "Updated bio",
                "Mysore",
                false
        );

        WorkerProfile updated = workerProfileService.updateProfile(workerA.getId(), updateRequest);

        assertNotNull(updated);
        assertEquals(profileA.getId(), updated.getId()); // ID preserved
        assertEquals(workerA.getId(), updated.getUserId()); // Ownership preserved
        assertEquals(5, updated.getExperienceYears());
        assertEquals(35.0, updated.getHourlyRate());
        assertEquals("Mysore", updated.getLocation());
        assertFalse(updated.isAvailable());
        assertEquals(List.of("Plumbing", "Electrical"), updated.getSkills());
        verify(workerProfileRepository, times(1)).save(profileA);
    }

    @Test
    @DisplayName("Scenario 4: Worker B cannot retrieve Worker A's private profile -> HTTP 403")
    void workerB_CannotRetrieveWorkerAProfile() {
        authenticateUser(workerB);

        AccessDeniedException exception = assertThrows(
                AccessDeniedException.class,
                () -> workerProfileService.getProfileByUserId(workerA.getId())
        );

        assertEquals("You are not authorized to view this profile", exception.getMessage());
        verify(workerProfileRepository, never()).findByUserId(anyString());
    }

    @Test
    @DisplayName("Scenario 5: Worker B cannot update Worker A's profile -> HTTP 403")
    void workerB_CannotUpdateWorkerAProfile() {
        authenticateUser(workerB);

        WorkerProfileRequest updateRequest = new WorkerProfileRequest(
                List.of("Hacking"), 1, 10.0, "Malicious", "Nowhere", false
        );

        AccessDeniedException exception = assertThrows(
                AccessDeniedException.class,
                () -> workerProfileService.updateProfile(workerA.getId(), updateRequest)
        );

        assertEquals("You are not authorized to modify this profile", exception.getMessage());
        verify(workerProfileRepository, never()).save(any(WorkerProfile.class));
    }

    @Test
    @DisplayName("Scenario 6: Worker B cannot delete Worker A's profile -> HTTP 403")
    void workerB_CannotDeleteWorkerAProfile() {
        authenticateUser(workerB);

        AccessDeniedException exception = assertThrows(
                AccessDeniedException.class,
                () -> workerProfileService.deleteProfile(workerA.getId())
        );

        assertEquals("You are not authorized to delete this profile", exception.getMessage());
        verify(workerProfileRepository, never()).delete(any(WorkerProfile.class));
    }

    @Test
    @DisplayName("Scenario 7: Employer cannot create, update, or delete a worker profile -> HTTP 403")
    void employer_CannotCreateOrModifyWorkerProfile() {
        authenticateUser(employer);

        WorkerProfileRequest request = new WorkerProfileRequest(
                List.of("Carpentry"), 2, 20.0, "Bio", "Bangalore", true
        );

        // Cannot create
        AccessDeniedException createEx = assertThrows(
                AccessDeniedException.class,
                () -> workerProfileService.createProfile(request, employer.getId())
        );
        assertEquals("Only workers can create a worker profile", createEx.getMessage());

        // Cannot update
        AccessDeniedException updateEx = assertThrows(
                AccessDeniedException.class,
                () -> workerProfileService.updateProfile(workerA.getId(), request)
        );
        assertEquals("Only workers can modify worker profiles", updateEx.getMessage());

        // Cannot delete
        AccessDeniedException deleteEx = assertThrows(
                AccessDeniedException.class,
                () -> workerProfileService.deleteProfile(workerA.getId())
        );
        assertEquals("Only workers can delete worker profiles", deleteEx.getMessage());

        verify(workerProfileRepository, never()).save(any(WorkerProfile.class));
        verify(workerProfileRepository, never()).delete(any(WorkerProfile.class));
    }

    @Test
    @DisplayName("Scenario 8: Unauthenticated user cannot access protected operations")
    void unauthenticatedUser_CannotAccessWorkerProfile() {
        SecurityContextHolder.clearContext();

        WorkerProfileRequest request = new WorkerProfileRequest(
                List.of("Plumbing"), 2, 20.0, "Bio", "Bangalore", true
        );

        assertThrows(AccessDeniedException.class, () -> workerProfileService.createProfile(request, null));
        assertThrows(AccessDeniedException.class, () -> workerProfileService.getProfileByUserId("workerA_id"));
        assertThrows(AccessDeniedException.class, () -> workerProfileService.updateProfile("workerA_id", request));
        assertThrows(AccessDeniedException.class, () -> workerProfileService.deleteProfile("workerA_id"));
    }

    @Test
    @DisplayName("Scenario 9: Request containing another user's workerId cannot change profile ownership")
    void requestContainingAnotherWorkerId_CannotChangeProfileOwnership() {
        authenticateUser(workerA);
        when(workerProfileRepository.existsByUserId(workerA.getId())).thenReturn(false);
        when(workerProfileRepository.save(any(WorkerProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Client passes Worker B's ID in body
        WorkerProfileRequest requestWithSpoofedId = new WorkerProfileRequest(
                List.of("Electrical"), 3, 28.0, "Electrician", "Bangalore", true
        );
        requestWithSpoofedId.setUserId("workerB_id"); // Attacker tries to impersonate or create for Worker B

        WorkerProfile created = workerProfileService.createProfile(requestWithSpoofedId, null);

        assertNotNull(created);
        assertEquals(workerA.getId(), created.getUserId(), "Profile userId must be bound to authenticated worker, ignoring body userId");

        // If workerA explicitly passes workerB_id in path:
        AccessDeniedException pathEx = assertThrows(
                AccessDeniedException.class,
                () -> workerProfileService.createProfile(requestWithSpoofedId, "workerB_id")
        );
        assertEquals("You are not authorized to create a profile for another user", pathEx.getMessage());
    }

    @Test
    @DisplayName("Scenario 11: Duplicate profile creation throws 409 Conflict")
    void duplicateProfileCreation_ThrowsConflictException() {
        authenticateUser(workerA);
        when(workerProfileRepository.existsByUserId(workerA.getId())).thenReturn(true);

        WorkerProfileRequest request = new WorkerProfileRequest(
                List.of("Plumbing"), 3, 25.0, "Plumber", "Bangalore", true
        );

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> workerProfileService.createProfile(request, workerA.getId())
        );

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        assertTrue(ex.getReason().contains("Worker profile already exists"));
        verify(workerProfileRepository, never()).save(any(WorkerProfile.class));
    }

    @Test
    @DisplayName("Admin can view and update any worker profile")
    void admin_CanViewAndUpdateWorkerProfile() {
        authenticateUser(admin);
        when(workerProfileRepository.findByUserId(workerA.getId())).thenReturn(Optional.of(profileA));
        when(workerProfileRepository.save(any(WorkerProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Admin retrieves Worker A's profile
        WorkerProfile viewed = workerProfileService.getProfileByUserId(workerA.getId());
        assertNotNull(viewed);

        // Admin updates Worker A's profile
        WorkerProfileRequest adminUpdateRequest = new WorkerProfileRequest(
                List.of("Plumbing", "Certified Master"), 10, 50.0, "Admin verified", "Bangalore", true
        );
        WorkerProfile updated = workerProfileService.updateProfile(workerA.getId(), adminUpdateRequest);
        assertNotNull(updated);
        assertEquals(10, updated.getExperienceYears());
        verify(workerProfileRepository, times(1)).save(profileA);
    }
}
