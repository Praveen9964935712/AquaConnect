package com.aquaconnect.backend.entity;

import java.time.Instant;
import java.util.UUID;

import com.aquaconnect.backend.enums.ResolutionDecision;

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
@Table(name = "resolution_confirmations")
public class ResolutionConfirmation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "incident_id", nullable = false, foreignKey = @ForeignKey(name = "fk_resolution_confirmations_incident"))
    private Incident incident;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "citizen_id", nullable = false, foreignKey = @ForeignKey(name = "fk_resolution_confirmations_citizen"))
    private User citizen;

    @Enumerated(EnumType.STRING)
    @Column(name = "decision", nullable = false, length = 16)
    private ResolutionDecision decision;

    @Column(name = "comment", length = 2000)
    private String comment;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected ResolutionConfirmation() { }

    public ResolutionConfirmation(Incident incident, User citizen, ResolutionDecision decision, String comment) {
        this.incident = incident;
        this.citizen = citizen;
        this.decision = decision;
        this.comment = comment;
    }

    @PrePersist
    protected void onCreate() { createdAt = Instant.now(); }

    public UUID getId() { return id; }
    public Incident getIncident() { return incident; }
    public User getCitizen() { return citizen; }
    public ResolutionDecision getDecision() { return decision; }
    public String getComment() { return comment; }
    public Instant getCreatedAt() { return createdAt; }
}
