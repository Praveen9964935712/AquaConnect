package com.aquaconnect.backend.dto.incident;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.aquaconnect.backend.entity.Incident;
import com.aquaconnect.backend.enums.IncidentCategory;
import com.aquaconnect.backend.enums.IncidentPriority;
import com.aquaconnect.backend.enums.IncidentSource;
import com.aquaconnect.backend.enums.IncidentStatus;
import com.aquaconnect.backend.enums.LocationSource;

public record IncidentResponse(
        UUID id,
        IncidentCategory category,
        String description,
        IncidentSource source,
        BigDecimal latitude,
        BigDecimal longitude,
        LocationSource locationSource,
        BigDecimal locationAccuracy,
        Instant reportedAt,
        IncidentStatus status,
        IncidentPriority priority,
        Instant createdAt,
        Instant updatedAt) {

    public static IncidentResponse from(Incident incident) {
        return new IncidentResponse(
                incident.getId(),
                incident.getCategory(),
                incident.getDescription(),
                incident.getSource(),
                incident.getLatitude(),
                incident.getLongitude(),
                incident.getLocationSource(),
                incident.getLocationAccuracy(),
                incident.getReportedAt(),
                incident.getStatus(),
                incident.getPriority(),
                incident.getCreatedAt(),
                incident.getUpdatedAt());
    }
}
