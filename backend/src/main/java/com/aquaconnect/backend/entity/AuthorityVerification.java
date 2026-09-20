package com.aquaconnect.backend.entity;

import java.time.Instant;
import java.util.UUID;

import com.aquaconnect.backend.enums.VerificationDecision;

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
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "authority_verifications")
public class AuthorityVerification {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "work_order_id", nullable = false, foreignKey = @ForeignKey(name = "fk_authority_verifications_work_order"))
    private WorkOrder workOrder;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "incident_id", nullable = false, foreignKey = @ForeignKey(name = "fk_authority_verifications_incident"))
    private Incident incident;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "verified_by_user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_authority_verifications_user"))
    private User verifiedBy;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "decision", nullable = false, length = 16)
    private VerificationDecision decision;

    @Size(max = 2000)
    @Column(name = "notes", length = 2000)
    private String notes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected AuthorityVerification() { }

    public AuthorityVerification(WorkOrder workOrder, Incident incident, User verifiedBy,
            VerificationDecision decision, String notes) {
        this.workOrder = workOrder;
        this.incident = incident;
        this.verifiedBy = verifiedBy;
        this.decision = decision;
        this.notes = notes;
    }

    @PrePersist
    protected void onCreate() { createdAt = Instant.now(); }

    public UUID getId() { return id; }
    public WorkOrder getWorkOrder() { return workOrder; }
    public Incident getIncident() { return incident; }
    public User getVerifiedBy() { return verifiedBy; }
    public VerificationDecision getDecision() { return decision; }
    public String getNotes() { return notes; }
    public Instant getCreatedAt() { return createdAt; }
}
