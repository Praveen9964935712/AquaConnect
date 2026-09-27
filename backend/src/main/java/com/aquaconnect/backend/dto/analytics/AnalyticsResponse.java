package com.aquaconnect.backend.dto.analytics;

import java.util.List;
import java.util.Map;

public record AnalyticsResponse(
        long totalIncidents,
        long openIncidents,
        long submittedIncidents,
        long verifiedIncidents,
        long assignedIncidents,
        long inProgressIncidents,
        long resolvedIncidents,
        long closedIncidents,
        long reopenedIncidents,
        Map<String, Long> incidentsByStatus,
        Map<String, Long> incidentsByPriority,
        Map<String, Long> incidentsByCategory,
        Map<String, Long> incidentsBySource,
        Map<String, Long> workOrdersByStatus,
        ResolutionSummary resolutionSummary,
        List<EngineerWorkloadSummary> engineerWorkload,
        List<DailyIncidentTrendPoint> dailyIncidentTrend,
        Double averageCompletionSeconds) {
}
