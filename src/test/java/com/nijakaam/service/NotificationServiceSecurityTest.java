package com.nijakaam.service;

import com.nijakaam.dto.response.NotificationResponse;
import com.nijakaam.dto.response.PagedResponse;
import com.nijakaam.entity.Notification;
import com.nijakaam.entity.User;
import com.nijakaam.enums.NotificationType;
import com.nijakaam.enums.Role;
import com.nijakaam.exception.ResourceNotFoundException;
import com.nijakaam.repository.NotificationRepository;
import com.nijakaam.repository.UserRepository;
import com.nijakaam.service.impl.NotificationServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
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
class NotificationServiceSecurityTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    private User userA;
    private User userB;
    private Notification notificationA;

    @BeforeEach
    void setUp() {
        userA = new User("User A", "userA@nijakaam.com", "pass123", "111", Role.WORKER);
        userA.setId("userA_id");

        userB = new User("User B", "userB@nijakaam.com", "pass123", "222", Role.EMPLOYER);
        userB.setId("userB_id");

        notificationA = new Notification("userA_id", "New Message", "Hello A", NotificationType.NEW_JOB_APPLICATION, "app_1");
        notificationA.setId("notif_A");
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
    @DisplayName("Users can view their own notifications successfully")
    void user_CanViewOwnNotifications() {
        authenticateUser(userA);

        Page<Notification> page = new PageImpl<>(List.of(notificationA));
        when(notificationRepository.findByRecipientId(eq("userA_id"), any(Pageable.class))).thenReturn(page);

        PagedResponse<NotificationResponse> response = notificationService.getMyNotifications(null, 0, 10);

        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        assertEquals("userA_id", response.getContent().get(0).getRecipientId());
    }

    @Test
    @DisplayName("Test 5: Users cannot view/mark as read other users' notifications -> HTTP 403 Forbidden")
    void userB_CannotMarkAsRead_UserANotification() {
        authenticateUser(userB);

        when(notificationRepository.findById("notif_A")).thenReturn(Optional.of(notificationA));

        AccessDeniedException ex = assertThrows(
                AccessDeniedException.class,
                () -> notificationService.markAsRead("notif_A")
        );

        assertEquals("You are not authorized to access this notification", ex.getMessage());
        assertFalse(notificationA.isRead());
    }

    @Test
    @DisplayName("Test 6: Users cannot delete other users' notifications -> HTTP 403 Forbidden")
    void userB_CannotDelete_UserANotification() {
        authenticateUser(userB);

        when(notificationRepository.findById("notif_A")).thenReturn(Optional.of(notificationA));

        AccessDeniedException ex = assertThrows(
                AccessDeniedException.class,
                () -> notificationService.deleteNotification("notif_A")
        );

        assertEquals("You are not authorized to delete this notification", ex.getMessage());
        verify(notificationRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Test 7: Read/unread status works correctly (mark as read & mark all as read)")
    void readStatus_WorksCorrectly() {
        authenticateUser(userA);

        when(notificationRepository.findById("notif_A")).thenReturn(Optional.of(notificationA));
        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> invocation.getArgument(0));

        NotificationResponse response = notificationService.markAsRead("notif_A");

        assertTrue(response.isRead());

        // Mark all as read
        when(notificationRepository.findByRecipientIdAndIsRead("userA_id", false)).thenReturn(List.of(notificationA));
        notificationService.markAllAsRead();

        verify(notificationRepository, times(1)).saveAll(any());
    }

    @Test
    @DisplayName("Test 8: Unread count is accurate")
    void unreadCount_IsAccurate() {
        authenticateUser(userA);

        when(notificationRepository.countByRecipientIdAndIsRead("userA_id", false)).thenReturn(4L);

        long count = notificationService.getMyUnreadCount();

        assertEquals(4L, count);
    }

    @Test
    @DisplayName("Test 9: Invalid pagination returns HTTP 400 Bad Request")
    void invalidPagination_ThrowsBadRequest() {
        authenticateUser(userA);

        // Negative page
        ResponseStatusException ex1 = assertThrows(
                ResponseStatusException.class,
                () -> notificationService.getMyNotifications(null, -1, 10)
        );
        assertEquals(HttpStatus.BAD_REQUEST, ex1.getStatusCode());

        // Size <= 0
        ResponseStatusException ex2 = assertThrows(
                ResponseStatusException.class,
                () -> notificationService.getMyNotifications(null, 0, 0)
        );
        assertEquals(HttpStatus.BAD_REQUEST, ex2.getStatusCode());

        // Size > 100
        ResponseStatusException ex3 = assertThrows(
                ResponseStatusException.class,
                () -> notificationService.getMyNotifications(null, 0, 101)
        );
        assertEquals(HttpStatus.BAD_REQUEST, ex3.getStatusCode());
    }

    @Test
    @DisplayName("Test 10: Unauthenticated access is denied")
    void unauthenticatedAccess_ThrowsAccessDenied() {
        SecurityContextHolder.clearContext();

        assertThrows(AccessDeniedException.class, () -> notificationService.getMyNotifications(null, 0, 10));
        assertThrows(AccessDeniedException.class, () -> notificationService.getMyUnreadCount());
        assertThrows(AccessDeniedException.class, () -> notificationService.markAsRead("notif_A"));
        assertThrows(AccessDeniedException.class, () -> notificationService.markAllAsRead());
        assertThrows(AccessDeniedException.class, () -> notificationService.deleteNotification("notif_A"));
    }

    @Test
    @DisplayName("Test 12: Duplicate notifications are prevented where appropriate")
    void duplicateNotifications_ArePrevented() {
        // Notification already exists for this recipient, type, and entity
        when(notificationRepository.existsByRecipientIdAndTypeAndRelatedEntityId("userA_id", NotificationType.NEW_JOB_APPLICATION, "app_1"))
                .thenReturn(true);

        notificationService.sendNotification("userA_id", "Title", "Message", NotificationType.NEW_JOB_APPLICATION, "app_1");

        // Repository save should NOT be called
        verify(notificationRepository, never()).save(any(Notification.class));
    }
}
