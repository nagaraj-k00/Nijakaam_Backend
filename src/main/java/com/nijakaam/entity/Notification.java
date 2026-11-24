package com.nijakaam.entity;

import com.nijakaam.enums.NotificationType;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "notifications")
@CompoundIndex(name = "recipient_created_idx", def = "{'recipientId': 1, 'createdAt': -1}")
@CompoundIndex(name = "recipient_isRead_idx", def = "{'recipientId': 1, 'isRead': 1}")
@CompoundIndex(name = "recipient_type_entity_idx", def = "{'recipientId': 1, 'type': 1, 'relatedEntityId': 1}")
public class Notification {

    @Id
    private String id;

    private String recipientId;
    private String title;
    private String message;
    private NotificationType type;
    private String relatedEntityId;
    private boolean isRead = false;
    private LocalDateTime createdAt = LocalDateTime.now();

    public Notification() {
    }

    public Notification(String recipientId, String title, String message,
                        NotificationType type, String relatedEntityId) {
        this.recipientId = recipientId;
        this.title = title;
        this.message = message;
        this.type = type;
        this.relatedEntityId = relatedEntityId;
        this.isRead = false;
        this.createdAt = LocalDateTime.now();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRecipientId() {
        return recipientId;
    }

    public void setRecipientId(String recipientId) {
        this.recipientId = recipientId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public NotificationType getType() {
        return type;
    }

    public void setType(NotificationType type) {
        this.type = type;
    }

    public String getRelatedEntityId() {
        return relatedEntityId;
    }

    public void setRelatedEntityId(String relatedEntityId) {
        this.relatedEntityId = relatedEntityId;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
