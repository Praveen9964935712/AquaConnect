package com.aquaconnect.backend.dto.incident;

import com.aquaconnect.backend.enums.IncidentPriority;

public record PriorityRecommendation(
        IncidentPriority priority,
        String reason) {
}
