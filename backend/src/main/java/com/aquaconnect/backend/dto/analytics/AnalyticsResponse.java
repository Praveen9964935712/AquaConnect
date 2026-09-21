package com.aquaconnect.backend.dto.analytics;

import java.util.Map;

public record AnalyticsResponse(
        long totalIncidents,
        long openIncidents,
        long resolvedIncidents,
        long closedIncidents,
        long reopenedIncidents,
        Map<String, Long> incidentsByStatus,
        Map<String, Long> incidentsByPriority,
        Map<String, Long> incidentsByCategory,
        Map<String, Long> incidentsBySource,
        Map<String, Long> workOrdersByStatus,
        Double averageCompletionSeconds) {
}
