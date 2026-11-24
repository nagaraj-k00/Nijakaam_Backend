package com.nijakaam.controller;

import com.nijakaam.dto.response.NotificationResponse;
import com.nijakaam.dto.response.PagedResponse;
import com.nijakaam.enums.NotificationType;
import com.nijakaam.exception.GlobalExceptionHandler;
import com.nijakaam.exception.ResourceNotFoundException;
import com.nijakaam.service.NotificationService;
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

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class NotificationControllerTest {

    private MockMvc mockMvc;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private NotificationController notificationController;

    private NotificationResponse sampleResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(notificationController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        sampleResponse = new NotificationResponse(
                "notif_1",
                "user_1",
                "New Job Application",
                "A worker applied for your job",
                NotificationType.NEW_JOB_APPLICATION,
                "app_1",
                false,
                LocalDateTime.now()
        );
    }

    @Test
    @DisplayName("GET /api/notifications returns 200 OK with paginated notifications")
    void getNotifications_Success_Returns200() throws Exception {
        PagedResponse<NotificationResponse> pagedResponse = new PagedResponse<>(
                List.of(sampleResponse),
                0,
                10,
                1L,
                1,
                true
        );

        when(notificationService.getMyNotifications(isNull(), eq(0), eq(10))).thenReturn(pagedResponse);

        mockMvc.perform(get("/api/notifications")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].id").value("notif_1"))
                .andExpect(jsonPath("$.data.content[0].title").value("New Job Application"))
                .andExpect(jsonPath("$.data.content[0].type").value("NEW_JOB_APPLICATION"));
    }

    @Test
    @DisplayName("GET /api/notifications/unread-count returns 200 OK with unread count")
    void getUnreadCount_Returns200() throws Exception {
        when(notificationService.getMyUnreadCount()).thenReturn(5L);

        mockMvc.perform(get("/api/notifications/unread-count")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.unreadCount").value(5));
    }

    @Test
    @DisplayName("POST /api/notifications/{id}/read marks notification as read")
    void markAsRead_Returns200() throws Exception {
        sampleResponse.setRead(true);
        when(notificationService.markAsRead("notif_1")).thenReturn(sampleResponse);

        mockMvc.perform(post("/api/notifications/notif_1/read")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.read").value(true));
    }

    @Test
    @DisplayName("PATCH /api/notifications/read-all marks all notifications as read")
    void markAllAsRead_Returns200() throws Exception {
        doNothing().when(notificationService).markAllAsRead();

        mockMvc.perform(patch("/api/notifications/read-all")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("All notifications marked as read"));
    }

    @Test
    @DisplayName("DELETE /api/notifications/{id} deletes notification for owner")
    void deleteNotification_Returns200() throws Exception {
        doNothing().when(notificationService).deleteNotification("notif_1");

        mockMvc.perform(delete("/api/notifications/notif_1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Notification deleted successfully"));
    }

    @Test
    @DisplayName("DELETE /api/notifications/{id} returns 403 Forbidden when unauthorized")
    void deleteNotification_Unauthorized_Returns403() throws Exception {
        doThrow(new AccessDeniedException("You are not authorized to delete this notification"))
                .when(notificationService).deleteNotification("notif_1");

        mockMvc.perform(delete("/api/notifications/notif_1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("You are not authorized to delete this notification"));
    }

    @Test
    @DisplayName("POST /api/notifications/{id}/read returns 404 Not Found when notification does not exist")
    void markAsRead_NotFound_Returns404() throws Exception {
        when(notificationService.markAsRead("non_existent"))
                .thenThrow(new ResourceNotFoundException("Notification not found with id: non_existent"));

        mockMvc.perform(post("/api/notifications/non_existent/read")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("GET /api/notifications with invalid pagination returns 400 Bad Request")
    void getNotifications_InvalidPagination_Returns400() throws Exception {
        when(notificationService.getMyNotifications(any(), eq(-1), anyInt()))
                .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Page index must not be less than zero"));

        mockMvc.perform(get("/api/notifications")
                        .param("page", "-1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Page index must not be less than zero"));
    }
}
