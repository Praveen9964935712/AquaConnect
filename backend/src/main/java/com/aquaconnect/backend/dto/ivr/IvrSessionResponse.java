package com.aquaconnect.backend.dto.ivr;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.aquaconnect.backend.entity.IvrSession;

public record IvrSessionResponse(UUID id, String callerIdentifier, String language, String state, String selectedCategory,
        String description, UUID incidentId, BigDecimal latitude, BigDecimal longitude, String locationSource,
        BigDecimal locationAccuracy, String sessionStatus, Instant createdAt, Instant updatedAt) {
    public static IvrSessionResponse from(IvrSession session) {
        return new IvrSessionResponse(session.getId(), session.getCallerIdentifier(), name(session.getLanguage()),
                name(session.getState()), name(session.getSelectedCategory()), session.getDescription(), session.getIncidentId(),
                session.getLatitude(), session.getLongitude(), name(session.getLocationSource()), session.getLocationAccuracy(),
                name(session.getSessionStatus()), session.getCreatedAt(), session.getUpdatedAt());
    }
    private static String name(Enum<?> value) { return value == null ? null : value.name(); }
}
