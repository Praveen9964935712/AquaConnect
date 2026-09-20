package com.aquaconnect.backend.entity;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "supporting_reports", uniqueConstraints = @UniqueConstraint(
        name = "uk_supporting_reports_pair",
        columnNames = {"primary_incident_id", "supporting_incident_id"}))
public class SupportingReport {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "primary_incident_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_supporting_reports_primary"))
    private Incident primaryIncident;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "supporting_incident_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_supporting_reports_supporting"))
    private Incident supportingIncident;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected SupportingReport() {
    }

    public SupportingReport(Incident primaryIncident, Incident supportingIncident) {
        this.primaryIncident = primaryIncident;
        this.supportingIncident = supportingIncident;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }

    public UUID getId() { return id; }
    public Incident getPrimaryIncident() { return primaryIncident; }
    public Incident getSupportingIncident() { return supportingIncident; }
    public Instant getCreatedAt() { return createdAt; }
}
