package com.aquaconnect.backend.entity;

import java.time.Instant;
import java.util.UUID;

import com.aquaconnect.backend.enums.IvrLanguage;
import com.aquaconnect.backend.enums.IvrEscalationStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "ivr_escalation_requests")
public class IvrEscalationRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "session_id", nullable = false)
    private UUID sessionId;

    @Column(name = "caller_identifier", nullable = false, length = 100)
    private String callerIdentifier;

    @Enumerated(EnumType.STRING)
    @Column(name = "language", length = 16)
    private IvrLanguage language;

    @Column(name = "reason", length = 1000)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 24)
    private IvrEscalationStatus status = IvrEscalationStatus.REQUESTED;

    @Column(name = "assigned_operator_id")
    private UUID assignedOperatorId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    protected IvrEscalationRequest() {
    }

    public IvrEscalationRequest(UUID sessionId, String callerIdentifier, IvrLanguage language, String reason) {
        this.sessionId = sessionId;
        this.callerIdentifier = callerIdentifier;
        this.language = language;
        this.reason = reason;
    }

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public UUID getSessionId() { return sessionId; }
    public String getCallerIdentifier() { return callerIdentifier; }
    public IvrLanguage getLanguage() { return language; }
    public String getReason() { return reason; }
    public IvrEscalationStatus getStatus() { return status; }
    public UUID getAssignedOperatorId() { return assignedOperatorId; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public Instant getCompletedAt() { return completedAt; }

    public void setStatus(IvrEscalationStatus status) { this.status = status; }
    public void setAssignedOperatorId(UUID assignedOperatorId) { this.assignedOperatorId = assignedOperatorId; }
    public void setCompletedAt(Instant completedAt) { this.completedAt = completedAt; }
}
