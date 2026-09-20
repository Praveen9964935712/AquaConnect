package com.aquaconnect.backend.service;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aquaconnect.backend.dto.incident.PriorityRecommendation;
import com.aquaconnect.backend.entity.Incident;
import com.aquaconnect.backend.enums.IncidentCategory;
import com.aquaconnect.backend.enums.IncidentPriority;
import com.aquaconnect.backend.exception.ResourceNotFoundException;
import com.aquaconnect.backend.repository.IncidentRepository;
import com.aquaconnect.backend.repository.SupportingReportRepository;

@Service
public class IncidentPriorityService {

    private static final Duration AGE_THRESHOLD = Duration.ofHours(24);

    private final IncidentRepository incidentRepository;
    private final SupportingReportRepository supportingReportRepository;

    public IncidentPriorityService(IncidentRepository incidentRepository,
            SupportingReportRepository supportingReportRepository) {
        this.incidentRepository = incidentRepository;
        this.supportingReportRepository = supportingReportRepository;
    }

    @Transactional(readOnly = true)
    public PriorityRecommendation recommend(UUID incidentId) {
        Incident incident = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new ResourceNotFoundException("Incident not found"));
        long supportingReports = supportingReportRepository.countByPrimaryIncidentId(incidentId);
        IncidentPriority priority = basePriority(incident.getCategory());
        StringBuilder reason = new StringBuilder(baseReason(incident.getCategory()));

        if (supportingReports >= 3) {
            priority = raise(priority);
            reason.append("; ").append(supportingReports).append(" supporting reports");
        }
        if (incident.getReportedAt() != null
                && incident.getReportedAt().isBefore(Instant.now().minus(AGE_THRESHOLD))) {
            priority = raise(priority);
            reason.append("; reported more than 24 hours ago");
        }
        return new PriorityRecommendation(priority, reason.toString());
    }

    private IncidentPriority basePriority(IncidentCategory category) {
        return switch (category) {
            case PIPE_BURST, CONTAMINATED_WATER -> IncidentPriority.HIGH;
            case PIPE_LEAK, NO_WATER_SUPPLY, LOW_WATER_PRESSURE, VALVE_ISSUE -> IncidentPriority.MEDIUM;
            case OTHER -> IncidentPriority.LOW;
        };
    }

    private String baseReason(IncidentCategory category) {
        return switch (category) {
            case PIPE_BURST -> "High-severity pipe burst";
            case CONTAMINATED_WATER -> "High-severity contaminated water report";
            case PIPE_LEAK -> "Medium-severity pipe leak";
            case NO_WATER_SUPPLY -> "Medium-severity no-water-supply report";
            case LOW_WATER_PRESSURE -> "Medium-severity low-water-pressure report";
            case VALVE_ISSUE -> "Medium-severity valve issue";
            case OTHER -> "No elevated severity signal identified";
        };
    }

    private IncidentPriority raise(IncidentPriority priority) {
        return switch (priority) {
            case LOW -> IncidentPriority.MEDIUM;
            case MEDIUM -> IncidentPriority.HIGH;
            case HIGH, CRITICAL -> IncidentPriority.CRITICAL;
        };
    }
}
