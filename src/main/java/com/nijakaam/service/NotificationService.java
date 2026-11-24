package com.nijakaam.service;

import com.nijakaam.dto.response.NotificationResponse;
import com.nijakaam.dto.response.PagedResponse;
import com.nijakaam.enums.NotificationType;

public interface NotificationService {

    void sendNotification(String recipientId, String title, String message, NotificationType type, String relatedEntityId);

    PagedResponse<NotificationResponse> getMyNotifications(Boolean isRead, int page, int size);

    long getMyUnreadCount();

    NotificationResponse markAsRead(String id);

    void markAllAsRead();

    void deleteNotification(String id);
}
