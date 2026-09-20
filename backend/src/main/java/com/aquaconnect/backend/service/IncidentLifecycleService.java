package com.aquaconnect.backend.service;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aquaconnect.backend.entity.Incident;
import com.aquaconnect.backend.enums.IncidentStatus;
import com.aquaconnect.backend.exception.InvalidIncidentTransitionException;
import com.aquaconnect.backend.exception.ResourceNotFoundException;
import com.aquaconnect.backend.repository.IncidentRepository;

@Service
public class IncidentLifecycleService {

    private static final Map<IncidentStatus, Set<IncidentStatus>> ALLOWED_TRANSITIONS = transitions();

    private final IncidentRepository incidentRepository;

    public IncidentLifecycleService(IncidentRepository incidentRepository) {
        this.incidentRepository = incidentRepository;
    }

    @Transactional
    public Incident transition(UUID incidentId, IncidentStatus targetStatus) {
        Incident incident = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new ResourceNotFoundException("Incident not found"));
        IncidentStatus currentStatus = incident.getStatus();
        if (!ALLOWED_TRANSITIONS.getOrDefault(currentStatus, Set.of()).contains(targetStatus)) {
            throw new InvalidIncidentTransitionException(currentStatus, targetStatus);
        }
        incident.setStatus(targetStatus);
        return incidentRepository.save(incident);
    }

    private static Map<IncidentStatus, Set<IncidentStatus>> transitions() {
        Map<IncidentStatus, Set<IncidentStatus>> transitions = new EnumMap<>(IncidentStatus.class);
        transitions.put(IncidentStatus.SUBMITTED, EnumSet.of(IncidentStatus.UNDER_VERIFICATION));
        transitions.put(IncidentStatus.UNDER_VERIFICATION, EnumSet.of(IncidentStatus.VERIFIED));
        transitions.put(IncidentStatus.VERIFIED, EnumSet.of(IncidentStatus.ASSIGNED));
        transitions.put(IncidentStatus.ASSIGNED, EnumSet.of(IncidentStatus.IN_PROGRESS));
        transitions.put(IncidentStatus.IN_PROGRESS, EnumSet.of(IncidentStatus.REPAIR_COMPLETED));
        transitions.put(IncidentStatus.REPAIR_COMPLETED, EnumSet.of(IncidentStatus.AUTHORITY_VERIFICATION));
        transitions.put(IncidentStatus.AUTHORITY_VERIFICATION, EnumSet.of(IncidentStatus.RESOLVED, IncidentStatus.IN_PROGRESS));
        transitions.put(IncidentStatus.RESOLVED, EnumSet.of(IncidentStatus.CLOSED, IncidentStatus.REOPENED));
        transitions.put(IncidentStatus.REOPENED, EnumSet.of(IncidentStatus.UNDER_VERIFICATION));
        return transitions;
    }
}
