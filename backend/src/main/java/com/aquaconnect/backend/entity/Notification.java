package com.aquaconnect.backend.entity;

import java.time.Instant;
import java.util.UUID;

import com.aquaconnect.backend.enums.NotificationType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recipient_user_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_notifications_recipient"))
    private User recipient;

    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type", nullable = false, length = 32)
    private NotificationType type;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "message", nullable = false, length = 2000)
    private String message;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "related_incident_id",
            foreignKey = @ForeignKey(name = "fk_notifications_incident"))
    private Incident relatedIncident;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "related_work_order_id",
            foreignKey = @ForeignKey(name = "fk_notifications_work_order"))
    private WorkOrder relatedWorkOrder;

    @Column(name = "read", nullable = false)
    private boolean read = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "read_at")
    private Instant readAt;

    protected Notification() { }

    public Notification(User recipient, NotificationType type, String title, String message,
            Incident relatedIncident, WorkOrder relatedWorkOrder) {
        this.recipient = recipient;
        this.type = type;
        this.title = title;
        this.message = message;
        this.relatedIncident = relatedIncident;
        this.relatedWorkOrder = relatedWorkOrder;
    }

    @PrePersist
    protected void onCreate() { createdAt = Instant.now(); }

    public UUID getId() { return id; }
    public User getRecipient() { return recipient; }
    public NotificationType getType() { return type; }
    public String getTitle() { return title; }
    public String getMessage() { return message; }
    public Incident getRelatedIncident() { return relatedIncident; }
    public WorkOrder getRelatedWorkOrder() { return relatedWorkOrder; }
    public boolean isRead() { return read; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getReadAt() { return readAt; }

    public void markRead() {
        read = true;
        readAt = Instant.now();
    }
}
