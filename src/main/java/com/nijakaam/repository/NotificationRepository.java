package com.nijakaam.repository;

import com.nijakaam.entity.Notification;
import com.nijakaam.enums.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends MongoRepository<Notification, String> {

    Page<Notification> findByRecipientId(String recipientId, Pageable pageable);

    Page<Notification> findByRecipientIdAndIsRead(String recipientId, boolean isRead, Pageable pageable);

    long countByRecipientIdAndIsRead(String recipientId, boolean isRead);

    boolean existsByRecipientIdAndTypeAndRelatedEntityId(String recipientId, NotificationType type, String relatedEntityId);

    List<Notification> findByRecipientIdAndIsRead(String recipientId, boolean isRead);
}
