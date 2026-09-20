package com.aquaconnect.backend.dto.incident;

import java.math.BigDecimal;
import java.util.UUID;

import com.aquaconnect.backend.enums.IncidentCategory;

public record IncidentCorrelationCandidate(
        UUID incidentId,
        IncidentCategory category,
        BigDecimal distanceMeters,
        String reason) {
}
