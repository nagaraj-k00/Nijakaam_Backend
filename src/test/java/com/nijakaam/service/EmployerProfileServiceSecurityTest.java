package com.nijakaam.service;

import com.nijakaam.dto.request.EmployerProfileRequest;
import com.nijakaam.entity.EmployerProfile;
import com.nijakaam.entity.User;
import com.nijakaam.enums.Role;
import com.nijakaam.repository.EmployerProfileRepository;
import com.nijakaam.repository.UserRepository;
import com.nijakaam.service.impl.EmployerProfileServiceImpl;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployerProfileServiceSecurityTest {

    @Mock
    private EmployerProfileRepository employerProfileRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private EmployerProfileServiceImpl employerProfileService;

    private User employerA;
    private User employerB;
    private User worker;
    private User admin;
    private EmployerProfile profileA;

    @BeforeEach
    void setUp() {
        employerA = new User("Employer A", "employerA@nijakaam.com", "pass123", "9998887771", Role.EMPLOYER);
        employerA.setId("employerA_id");

        employerB = new User("Employer B", "employerB@nijakaam.com", "pass123", "9998887772", Role.EMPLOYER);
        employerB.setId("employerB_id");

        worker = new User("Worker X", "worker@nijakaam.com", "pass123", "9998887773", Role.WORKER);
        worker.setId("worker_id");

        admin = new User("Admin User", "admin@nijakaam.com", "pass123", "9998887774", Role.ADMIN);
        admin.setId("admin_id");

        profileA = new EmployerProfile(
                "employerA_id",
                "Acme Corp",
                "Leading logistics solutions",
                "https://acme.com",
                "Bangalore",
                "Logistics"
        );
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
    @DisplayName("Scenario 1: Employer A creates an employer profile successfully")
    void employerA_CreatesProfile_Successfully() {
        authenticateUser(employerA);
        when(employerProfileRepository.existsByUserId(employerA.getId())).thenReturn(false);
        when(employerProfileRepository.save(any(EmployerProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        EmployerProfileRequest request = new EmployerProfileRequest(
                "Acme Corp",
                "Leading tech solutions",
                "https://acme.com",
                "Bangalore",
                "Information Technology"
        );

        EmployerProfile created = employerProfileService.createProfile(request);

        assertNotNull(created);
        assertEquals(employerA.getId(), created.getUserId());
        assertEquals("Acme Corp", created.getCompanyName());
        assertEquals("Leading tech solutions", created.getCompanyDescription());
        assertEquals("https://acme.com", created.getCompanyWebsite());
        assertEquals("Bangalore", created.getLocation());
        assertEquals("Information Technology", created.getIndustry());
        assertNotNull(created.getCreatedAt());
        assertNotNull(created.getUpdatedAt());
        verify(employerProfileRepository, times(1)).save(any(EmployerProfile.class));
    }

    @Test
    @DisplayName("Scenario 2: Employer A retrieves their own profile")
    void employerA_RetrievesOwnProfile_Successfully() {
        authenticateUser(employerA);
        when(employerProfileRepository.findByUserId(employerA.getId())).thenReturn(Optional.of(profileA));

        EmployerProfile retrieved = employerProfileService.getProfileByUserId(employerA.getId());

        assertNotNull(retrieved);
        assertEquals(profileA.getId(), retrieved.getId());
        assertEquals(employerA.getId(), retrieved.getUserId());
    }

    @Test
    @DisplayName("Scenario 2b: Employer A retrieves their own profile via getMyProfile() (/me endpoint)")
    void employerA_RetrievesOwnProfile_ViaGetMyProfile() {
        authenticateUser(employerA);
        when(employerProfileRepository.findByUserId(employerA.getId())).thenReturn(Optional.of(profileA));

        EmployerProfile retrieved = employerProfileService.getMyProfile();

        assertNotNull(retrieved);
        assertEquals(profileA.getId(), retrieved.getId());
        assertEquals(employerA.getId(), retrieved.getUserId());
    }

    @Test
    @DisplayName("Scenario 3: Employer A updates their own profile")
    void employerA_UpdatesOwnProfile_Successfully() {
        authenticateUser(employerA);
        when(employerProfileRepository.findByUserId(employerA.getId())).thenReturn(Optional.of(profileA));
        when(employerProfileRepository.save(any(EmployerProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        EmployerProfileRequest updateRequest = new EmployerProfileRequest(
                "Acme Global Inc",
                "Global tech enterprise",
                "https://acmeglobal.com",
                "Mumbai",
                "Enterprise Software"
        );

        EmployerProfile updated = employerProfileService.updateProfile(employerA.getId(), updateRequest);

        assertNotNull(updated);
        assertEquals(profileA.getId(), updated.getId()); // ID preserved
        assertEquals(employerA.getId(), updated.getUserId()); // Ownership preserved
        assertEquals("Acme Global Inc", updated.getCompanyName());
        assertEquals("Mumbai", updated.getLocation());
        assertEquals("https://acmeglobal.com", updated.getCompanyWebsite());
        verify(employerProfileRepository, times(1)).save(profileA);
    }

    @Test
    @DisplayName("Scenario 4: Employer B cannot retrieve Employer A's private profile -> HTTP 403")
    void employerB_CannotRetrieveEmployerAProfile() {
        authenticateUser(employerB);

        AccessDeniedException exception = assertThrows(
                AccessDeniedException.class,
                () -> employerProfileService.getProfileByUserId(employerA.getId())
        );

        assertEquals("You are not authorized to view this profile", exception.getMessage());
        verify(employerProfileRepository, never()).findByUserId(anyString());
    }

    @Test
    @DisplayName("Scenario 5: Employer B cannot update Employer A's profile -> HTTP 403")
    void employerB_CannotUpdateEmployerAProfile() {
        authenticateUser(employerB);

        EmployerProfileRequest updateRequest = new EmployerProfileRequest(
                "Hostile Takeover Ltd", "Malicious update", "https://hacked.com", "Nowhere", "Finance"
        );

        AccessDeniedException exception = assertThrows(
                AccessDeniedException.class,
                () -> employerProfileService.updateProfile(employerA.getId(), updateRequest)
        );

        assertEquals("You are not authorized to modify this profile", exception.getMessage());
        verify(employerProfileRepository, never()).save(any(EmployerProfile.class));
    }

    @Test
    @DisplayName("Scenario 6: Employer B cannot delete Employer A's profile -> HTTP 403")
    void employerB_CannotDeleteEmployerAProfile() {
        authenticateUser(employerB);

        AccessDeniedException exception = assertThrows(
                AccessDeniedException.class,
                () -> employerProfileService.deleteProfile(employerA.getId())
        );

        assertEquals("You are not authorized to delete this profile", exception.getMessage());
        verify(employerProfileRepository, never()).delete(any(EmployerProfile.class));
    }

    @Test
    @DisplayName("Scenario 7: Worker cannot create, update, or delete an employer profile -> HTTP 403")
    void worker_CannotCreateOrModifyEmployerProfile() {
        authenticateUser(worker);

        EmployerProfileRequest request = new EmployerProfileRequest(
                "Worker Inc", "Worker attempting employer profile", "https://worker.com", "Bangalore", "Services"
        );

        // Cannot create
        AccessDeniedException createEx = assertThrows(
                AccessDeniedException.class,
                () -> employerProfileService.createProfile(request, worker.getId())
        );
        assertEquals("Only employers can create an employer profile", createEx.getMessage());

        // Cannot update
        AccessDeniedException updateEx = assertThrows(
                AccessDeniedException.class,
                () -> employerProfileService.updateProfile(employerA.getId(), request)
        );
        assertEquals("Only employers can modify employer profiles", updateEx.getMessage());

        // Cannot delete
        AccessDeniedException deleteEx = assertThrows(
                AccessDeniedException.class,
                () -> employerProfileService.deleteProfile(employerA.getId())
        );
        assertEquals("Only employers can delete employer profiles", deleteEx.getMessage());

        verify(employerProfileRepository, never()).save(any(EmployerProfile.class));
        verify(employerProfileRepository, never()).delete(any(EmployerProfile.class));
    }

    @Test
    @DisplayName("Scenario 8: Unauthenticated user cannot access protected operations")
    void unauthenticatedUser_CannotAccessEmployerProfile() {
        SecurityContextHolder.clearContext();

        EmployerProfileRequest request = new EmployerProfileRequest(
                "Acme", "Desc", "https://acme.com", "Bangalore", "IT"
        );

        assertThrows(AccessDeniedException.class, () -> employerProfileService.createProfile(request, null));
        assertThrows(AccessDeniedException.class, () -> employerProfileService.getProfileByUserId("employerA_id"));
        assertThrows(AccessDeniedException.class, () -> employerProfileService.updateProfile("employerA_id", request));
        assertThrows(AccessDeniedException.class, () -> employerProfileService.deleteProfile("employerA_id"));
    }

    @Test
    @DisplayName("Scenario 9: Request containing another user's employerId cannot change profile ownership")
    void requestContainingAnotherEmployerId_CannotChangeProfileOwnership() {
        authenticateUser(employerA);
        when(employerProfileRepository.existsByUserId(employerA.getId())).thenReturn(false);
        when(employerProfileRepository.save(any(EmployerProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Client passes Employer B's ID in body
        EmployerProfileRequest requestWithSpoofedId = new EmployerProfileRequest(
                "Acme Corp", "Desc", "https://acme.com", "Bangalore", "IT"
        );
        requestWithSpoofedId.setUserId("employerB_id"); // Attacker tries to impersonate or create for Employer B

        EmployerProfile created = employerProfileService.createProfile(requestWithSpoofedId, null);

        assertNotNull(created);
        assertEquals(employerA.getId(), created.getUserId(), "Profile userId must be bound to authenticated employer, ignoring body userId");

        // If employerA explicitly passes employerB_id in path:
        AccessDeniedException pathEx = assertThrows(
                AccessDeniedException.class,
                () -> employerProfileService.createProfile(requestWithSpoofedId, "employerB_id")
        );
        assertEquals("You are not authorized to create a profile for another user", pathEx.getMessage());
    }

    @Test
    @DisplayName("Scenario 11: Duplicate profile creation throws 409 Conflict")
    void duplicateProfileCreation_ThrowsConflictException() {
        authenticateUser(employerA);
        when(employerProfileRepository.existsByUserId(employerA.getId())).thenReturn(true);

        EmployerProfileRequest request = new EmployerProfileRequest(
                "Acme Corp", "Desc", "https://acme.com", "Bangalore", "IT"
        );

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> employerProfileService.createProfile(request, employerA.getId())
        );

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        assertTrue(ex.getReason().contains("Employer profile already exists"));
        verify(employerProfileRepository, never()).save(any(EmployerProfile.class));
    }

    @Test
    @DisplayName("Scenario 12: Admin can view and update any employer profile")
    void admin_CanViewAndUpdateEmployerProfile() {
        authenticateUser(admin);
        when(employerProfileRepository.findByUserId(employerA.getId())).thenReturn(Optional.of(profileA));
        when(employerProfileRepository.save(any(EmployerProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Admin retrieves Employer A's profile
        EmployerProfile viewed = employerProfileService.getProfileByUserId(employerA.getId());
        assertNotNull(viewed);

        // Admin updates Employer A's profile
        EmployerProfileRequest adminUpdateRequest = new EmployerProfileRequest(
                "Acme Corp Verified", "Admin verified company", "https://acme.com", "Bangalore", "Enterprise Tech"
        );
        EmployerProfile updated = employerProfileService.updateProfile(employerA.getId(), adminUpdateRequest);
        assertNotNull(updated);
        assertEquals("Acme Corp Verified", updated.getCompanyName());
        verify(employerProfileRepository, times(1)).save(profileA);
    }
}
