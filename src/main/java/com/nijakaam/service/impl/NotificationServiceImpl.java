package com.nijakaam.service.impl;

import com.nijakaam.dto.response.NotificationResponse;
import com.nijakaam.dto.response.PagedResponse;
import com.nijakaam.entity.Notification;
import com.nijakaam.entity.User;
import com.nijakaam.enums.NotificationType;
import com.nijakaam.enums.Role;
import com.nijakaam.exception.ResourceNotFoundException;
import com.nijakaam.repository.NotificationRepository;
import com.nijakaam.repository.UserRepository;
import com.nijakaam.service.NotificationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationServiceImpl(NotificationRepository notificationRepository, UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null ||
                !authentication.isAuthenticated() ||
                "anonymousUser".equals(authentication.getPrincipal())) {
            throw new AccessDeniedException("User is not authenticated");
        }

        String email = authentication.getName();
        if (email == null || email.isBlank()) {
            throw new AccessDeniedException("Authenticated user identity is missing");
        }

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    @Override
    public void sendNotification(String recipientId, String title, String message,
                                 NotificationType type, String relatedEntityId) {
        if (recipientId == null || recipientId.isBlank()) {
            return;
        }

        // Avoid duplicate notification if operation is retried
        if (notificationRepository.existsByRecipientIdAndTypeAndRelatedEntityId(recipientId, type, relatedEntityId)) {
            return;
        }

        Notification notification = new Notification(recipientId, title, message, type, relatedEntityId);
        notificationRepository.save(notification);
    }

    @Override
    public PagedResponse<NotificationResponse> getMyNotifications(Boolean isRead, int page, int size) {
        User currentUser = getCurrentUser();

        validatePagination(page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Notification> notificationPage;
        if (isRead != null) {
            notificationPage = notificationRepository.findByRecipientIdAndIsRead(currentUser.getId(), isRead, pageable);
        } else {
            notificationPage = notificationRepository.findByRecipientId(currentUser.getId(), pageable);
        }

        List<NotificationResponse> content = notificationPage.getContent().stream()
                .map(NotificationResponse::fromEntity)
                .toList();

        return new PagedResponse<>(
                content,
                notificationPage.getNumber(),
                notificationPage.getSize(),
                notificationPage.getTotalElements(),
                notificationPage.getTotalPages(),
                notificationPage.isLast()
        );
    }

    @Override
    public long getMyUnreadCount() {
        User currentUser = getCurrentUser();
        return notificationRepository.countByRecipientIdAndIsRead(currentUser.getId(), false);
    }

    @Override
    public NotificationResponse markAsRead(String id) {
        User currentUser = getCurrentUser();

        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " + id));

        // Ownership enforcement: users can only view/modify their own notifications
        if (currentUser.getRole() != Role.ADMIN && !notification.getRecipientId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You are not authorized to access this notification");
        }

        notification.setRead(true);
        Notification updated = notificationRepository.save(notification);

        return NotificationResponse.fromEntity(updated);
    }

    @Override
    public void markAllAsRead() {
        User currentUser = getCurrentUser();

        List<Notification> unreadNotifications = notificationRepository.findByRecipientIdAndIsRead(currentUser.getId(), false);
        for (Notification notification : unreadNotifications) {
            notification.setRead(true);
        }

        notificationRepository.saveAll(unreadNotifications);
    }

    @Override
    public void deleteNotification(String id) {
        User currentUser = getCurrentUser();

        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " + id));

        // Ownership enforcement: users can only delete their own notifications
        if (currentUser.getRole() != Role.ADMIN && !notification.getRecipientId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You are not authorized to delete this notification");
        }

        notificationRepository.delete(notification);
    }

    private void validatePagination(int page, int size) {
        if (page < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Page index must not be less than zero");
        }
        if (size <= 0 || size > 100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Page size must be between 1 and 100");
        }
    }
}
