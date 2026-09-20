package com.aquaconnect.backend.service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aquaconnect.backend.dto.incident.IncidentCorrelationCandidate;
import com.aquaconnect.backend.entity.Incident;
import com.aquaconnect.backend.repository.IncidentRepository;

@Service
public class IncidentCorrelationService {

    private static final Duration RECENT_WINDOW = Duration.ofHours(24);
    private static final BigDecimal MAX_DISTANCE_METERS = new BigDecimal("1000");

    private final IncidentRepository incidentRepository;

    public IncidentCorrelationService(IncidentRepository incidentRepository) {
        this.incidentRepository = incidentRepository;
    }

    @Transactional(readOnly = true)
    public List<IncidentCorrelationCandidate> findPotentiallyRelated(UUID incidentId) {
        Incident target = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new IllegalArgumentException("Incident not found"));
        Instant cutoff = target.getReportedAt().minus(RECENT_WINDOW);
        return incidentRepository.findAll().stream()
                .filter(candidate -> !candidate.getId().equals(target.getId()))
                .filter(candidate -> candidate.getCategory() == target.getCategory())
                .filter(candidate -> candidate.getReportedAt() != null && !candidate.getReportedAt().isBefore(cutoff))
                .filter(candidate -> candidate.getStatus() != com.aquaconnect.backend.enums.IncidentStatus.CLOSED)
                .map(candidate -> toCandidate(target, candidate))
                .filter(candidate -> candidate != null)
                .toList();
    }

    private IncidentCorrelationCandidate toCandidate(Incident target, Incident candidate) {
        BigDecimal distance = distanceMeters(target, candidate);
        if (distance != null && distance.compareTo(MAX_DISTANCE_METERS) > 0) {
            return null;
        }
        String reason = distance == null
                ? "Same category and reported within 24 hours; location unavailable for distance comparison"
                : "Same category, reported within 24 hours, and within 1000 meters";
        return new IncidentCorrelationCandidate(candidate.getId(), candidate.getCategory(), distance, reason);
    }

    private BigDecimal distanceMeters(Incident first, Incident second) {
        if (first.getLatitude() == null || first.getLongitude() == null
                || second.getLatitude() == null || second.getLongitude() == null) {
            return null;
        }
        double latitudeDifference = Math.toRadians(second.getLatitude().doubleValue() - first.getLatitude().doubleValue());
        double longitudeDifference = Math.toRadians(second.getLongitude().doubleValue() - first.getLongitude().doubleValue());
        double meanLatitude = Math.toRadians((first.getLatitude().doubleValue() + second.getLatitude().doubleValue()) / 2);
        double x = longitudeDifference * Math.cos(meanLatitude);
        double y = latitudeDifference;
        return BigDecimal.valueOf(6_371_000d * Math.sqrt(x * x + y * y)).round(new MathContext(8));
    }
}
