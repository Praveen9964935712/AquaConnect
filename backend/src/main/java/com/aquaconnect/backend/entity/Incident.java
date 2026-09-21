package com.aquaconnect.backend.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.aquaconnect.backend.enums.IncidentCategory;
import com.aquaconnect.backend.enums.IncidentPriority;
import com.aquaconnect.backend.enums.IncidentSource;
import com.aquaconnect.backend.enums.IncidentStatus;
import com.aquaconnect.backend.enums.LocationSource;

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
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "incidents")
public class Incident {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "citizen_id", foreignKey = @ForeignKey(name = "fk_incidents_citizen"))
    private User citizen;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "source", nullable = false, length = 16)
    private IncidentSource source;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 32)
    private IncidentCategory category;

    @NotBlank
    @Size(max = 2000)
    @Column(name = "description", nullable = false, length = 2000)
    private String description;

    @Column(name = "caller_identifier", length = 100)
    private String callerIdentifier;

    @DecimalMin("-90.0")
    @DecimalMax("90.0")
    @Column(name = "latitude", precision = 9, scale = 6)
    private BigDecimal latitude;

    @DecimalMin("-180.0")
    @DecimalMax("180.0")
    @Column(name = "longitude", precision = 10, scale = 7)
    private BigDecimal longitude;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "location_source", nullable = false, length = 32)
    private LocationSource locationSource = LocationSource.UNKNOWN;

    @DecimalMin("0.0")
    @Column(name = "location_accuracy", precision = 12, scale = 3)
    private BigDecimal locationAccuracy;

    @NotNull
    @Column(name = "reported_at", nullable = false)
    private Instant reportedAt;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private IncidentStatus status = IncidentStatus.SUBMITTED;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false, length = 16)
    private IncidentPriority priority = IncidentPriority.LOW;

    @NotNull
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @NotNull
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Incident() {
    }

    public Incident(User citizen, IncidentSource source, IncidentCategory category, String description,
            BigDecimal latitude, BigDecimal longitude, LocationSource locationSource,
            BigDecimal locationAccuracy) {
        this.citizen = citizen;
        this.source = source;
        this.category = category;
        this.description = description;
        this.latitude = latitude;
        this.longitude = longitude;
        this.locationSource = locationSource == null ? LocationSource.UNKNOWN : locationSource;
        this.locationAccuracy = locationAccuracy;
    }

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        this.reportedAt = reportedAt == null ? now : reportedAt;
        this.status = status == null ? IncidentStatus.SUBMITTED : status;
        this.priority = priority == null ? IncidentPriority.LOW : priority;
        this.locationSource = locationSource == null ? LocationSource.UNKNOWN : locationSource;
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public User getCitizen() { return citizen; }
    public IncidentSource getSource() { return source; }
    public IncidentCategory getCategory() { return category; }
    public String getDescription() { return description; }
    public String getCallerIdentifier() { return callerIdentifier; }
    public BigDecimal getLatitude() { return latitude; }
    public BigDecimal getLongitude() { return longitude; }
    public LocationSource getLocationSource() { return locationSource; }
    public BigDecimal getLocationAccuracy() { return locationAccuracy; }
    public Instant getReportedAt() { return reportedAt; }
    public IncidentStatus getStatus() { return status; }
    public IncidentPriority getPriority() { return priority; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    public void setStatus(IncidentStatus status) { this.status = status; }
    public void setPriority(IncidentPriority priority) { this.priority = priority; }
    public void setCallerIdentifier(String callerIdentifier) { this.callerIdentifier = callerIdentifier; }
}
