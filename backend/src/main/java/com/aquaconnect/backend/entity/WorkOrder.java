package com.aquaconnect.backend.entity;

import java.time.Instant;
import java.util.UUID;

import com.aquaconnect.backend.enums.IncidentPriority;
import com.aquaconnect.backend.enums.WorkOrderStatus;

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
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "work_orders", uniqueConstraints = @UniqueConstraint(
        name = "uk_work_orders_incident", columnNames = "incident_id"))
public class WorkOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "incident_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_work_orders_incident"))
    private Incident incident;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_engineer_id",
            foreignKey = @ForeignKey(name = "fk_work_orders_engineer"))
    private User assignedEngineer;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    private WorkOrderStatus status = WorkOrderStatus.CREATED;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false, length = 16)
    private IncidentPriority priority;

    @NotNull
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @NotNull
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "assigned_at")
    private Instant assignedAt;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "inspection_notes", length = 2000)
    private String inspectionNotes;

    @Column(name = "observed_condition", length = 1000)
    private String observedCondition;

    @Column(name = "repair_notes", length = 2000)
    private String repairNotes;

    protected WorkOrder() {
    }

    public WorkOrder(Incident incident, IncidentPriority priority) {
        this.incident = incident;
        this.priority = priority;
    }

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
        status = status == null ? WorkOrderStatus.CREATED : status;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public Incident getIncident() { return incident; }
    public User getAssignedEngineer() { return assignedEngineer; }
    public WorkOrderStatus getStatus() { return status; }
    public IncidentPriority getPriority() { return priority; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public Instant getAssignedAt() { return assignedAt; }
    public Instant getStartedAt() { return startedAt; }
    public Instant getCompletedAt() { return completedAt; }
    public String getInspectionNotes() { return inspectionNotes; }
    public String getObservedCondition() { return observedCondition; }
    public String getRepairNotes() { return repairNotes; }

    public void assign(User engineer) {
        assignedEngineer = engineer;
        assignedAt = Instant.now();
        status = WorkOrderStatus.ASSIGNED;
    }

    public void setStatus(WorkOrderStatus status) {
        this.status = status;
        Instant now = Instant.now();
        if (status == WorkOrderStatus.IN_PROGRESS) {
            startedAt = now;
        } else if (status == WorkOrderStatus.COMPLETED) {
            completedAt = now;
        }
    }

    public void recordFindings(String inspectionNotes, String observedCondition, String repairNotes) {
        this.inspectionNotes = inspectionNotes;
        this.observedCondition = observedCondition;
        this.repairNotes = repairNotes;
    }
}
