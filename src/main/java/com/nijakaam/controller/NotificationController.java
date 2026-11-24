package com.nijakaam.controller;

import com.nijakaam.dto.response.ApiResponse;
import com.nijakaam.dto.response.NotificationResponse;
import com.nijakaam.dto.response.PagedResponse;
import com.nijakaam.dto.response.UnreadCountResponse;
import com.nijakaam.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    // Get notifications for authenticated user
    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<NotificationResponse>>> getNotifications(
            @RequestParam(required = false) Boolean isRead,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        PagedResponse<NotificationResponse> result = notificationService.getMyNotifications(isRead, page, size);
        return ResponseEntity.ok(ApiResponse.success("Notifications retrieved successfully", result));
    }

    // Get unread notification count for authenticated user
    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<UnreadCountResponse>> getUnreadCount() {
        long count = notificationService.getMyUnreadCount();
        return ResponseEntity.ok(ApiResponse.success("Unread count retrieved successfully", new UnreadCountResponse(count)));
    }

    // Mark single notification as read
    @PostMapping("/{id}/read")
    public ResponseEntity<ApiResponse<NotificationResponse>> markAsRead(@PathVariable String id) {
        NotificationResponse updated = notificationService.markAsRead(id);
        return ResponseEntity.ok(ApiResponse.success("Notification marked as read", updated));
    }

    // Mark all notifications as read for authenticated user
    @PatchMapping("/read-all")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead() {
        notificationService.markAllAsRead();
        return ResponseEntity.ok(ApiResponse.success("All notifications marked as read", null));
    }

    // Delete a notification
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteNotification(@PathVariable String id) {
        notificationService.deleteNotification(id);
        return ResponseEntity.ok(ApiResponse.success("Notification deleted successfully", null));
    }
}
