package com.aquaconnect.backend.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.aquaconnect.backend.enums.IncidentCategory;
import com.aquaconnect.backend.enums.IvrLanguage;
import com.aquaconnect.backend.enums.IvrSessionState;
import com.aquaconnect.backend.enums.IvrSessionStatus;
import com.aquaconnect.backend.enums.LocationSource;

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
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;

@Entity
@Table(name = "ivr_sessions")
public class IvrSession {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "caller_identifier", length = 100)
    private String callerIdentifier;

    @Enumerated(EnumType.STRING)
    @Column(name = "language", length = 16)
    private IvrLanguage language;

    @Enumerated(EnumType.STRING)
    @Column(name = "state", nullable = false, length = 32)
    private IvrSessionState state = IvrSessionState.START;

    @Enumerated(EnumType.STRING)
    @Column(name = "selected_category", length = 32)
    private IncidentCategory selectedCategory;

    @Column(name = "description", length = 2000)
    private String description;

    @Column(name = "incident_id")
    private UUID incidentId;

    @DecimalMin("-90.0") @DecimalMax("90.0")
    @Column(name = "latitude", precision = 9, scale = 6)
    private BigDecimal latitude;

    @DecimalMin("-180.0") @DecimalMax("180.0")
    @Column(name = "longitude", precision = 10, scale = 7)
    private BigDecimal longitude;

    @Enumerated(EnumType.STRING)
    @Column(name = "location_source", nullable = false, length = 32)
    private LocationSource locationSource = LocationSource.UNKNOWN;

    @DecimalMin("0.0")
    @Column(name = "location_accuracy", precision = 12, scale = 3)
    private BigDecimal locationAccuracy;

    @Enumerated(EnumType.STRING)
    @Column(name = "session_status", nullable = false, length = 16)
    private IvrSessionStatus sessionStatus = IvrSessionStatus.ACTIVE;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected IvrSession() {}
    public IvrSession(String callerIdentifier) { this.callerIdentifier = callerIdentifier; }

    @PrePersist protected void onCreate() { createdAt = Instant.now(); updatedAt = createdAt; }
    @PreUpdate protected void onUpdate() { updatedAt = Instant.now(); }

    public UUID getId() { return id; }
    public String getCallerIdentifier() { return callerIdentifier; }
    public IvrLanguage getLanguage() { return language; }
    public IvrSessionState getState() { return state; }
    public IncidentCategory getSelectedCategory() { return selectedCategory; }
    public String getDescription() { return description; }
    public UUID getIncidentId() { return incidentId; }
    public BigDecimal getLatitude() { return latitude; }
    public BigDecimal getLongitude() { return longitude; }
    public LocationSource getLocationSource() { return locationSource; }
    public BigDecimal getLocationAccuracy() { return locationAccuracy; }
    public IvrSessionStatus getSessionStatus() { return sessionStatus; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    public void selectLanguage(IvrLanguage language) { this.language = language; this.state = IvrSessionState.MAIN_MENU; }
    public void chooseReport() { state = IvrSessionState.REPORT_PROBLEM; }
    public void chooseCheckComplaint() { state = IvrSessionState.CHECK_COMPLAINT; }
    public void chooseWaterSupplyInfo() { state = IvrSessionState.WATER_SUPPLY_INFO; }
    public void chooseOperator() { state = IvrSessionState.OPERATOR_REQUEST; }
    public void repeatMenu() { state = IvrSessionState.MAIN_MENU; }
    public void details(IncidentCategory category, String description) { selectedCategory = category; this.description = description; state = IvrSessionState.CONFIRMATION; }
    public void location(BigDecimal latitude, BigDecimal longitude, LocationSource source, BigDecimal accuracy) { this.latitude = latitude; this.longitude = longitude; this.locationSource = source == null ? LocationSource.UNKNOWN : source; this.locationAccuracy = accuracy; }
    public void markIncidentCreated(UUID incidentId) { this.incidentId = incidentId; state = IvrSessionState.INCIDENT_CREATED; }
    public void complete() { state = IvrSessionState.COMPLETED; sessionStatus = IvrSessionStatus.COMPLETED; }
    public void fail() { state = IvrSessionState.FAILED; sessionStatus = IvrSessionStatus.FAILED; }
}
