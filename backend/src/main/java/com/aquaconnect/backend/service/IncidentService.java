package com.aquaconnect.backend.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aquaconnect.backend.dto.incident.IncidentCreateRequest;
import com.aquaconnect.backend.dto.incident.IncidentResponse;
import com.aquaconnect.backend.entity.Incident;
import com.aquaconnect.backend.entity.User;
import com.aquaconnect.backend.enums.IncidentSource;
import com.aquaconnect.backend.enums.LocationSource;
import com.aquaconnect.backend.exception.ResourceNotFoundException;
import com.aquaconnect.backend.repository.IncidentRepository;
import com.aquaconnect.backend.repository.UserRepository;

@Service
public class IncidentService {

    private final IncidentRepository incidentRepository;
    private final UserRepository userRepository;

    public IncidentService(IncidentRepository incidentRepository, UserRepository userRepository) {
        this.incidentRepository = incidentRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public IncidentResponse createForCitizen(UUID citizenId, IncidentCreateRequest request) {
        User citizen = userRepository.findById(citizenId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (request.source() != null && request.source() != IncidentSource.WEB) {
            throw new IllegalArgumentException("Citizen incidents must use the WEB source");
        }
        Incident incident = new Incident(
                citizen,
                IncidentSource.WEB,
                request.category(),
                request.description().trim(),
                request.latitude(),
                request.longitude(),
                request.locationSource() == null ? LocationSource.UNKNOWN : request.locationSource(),
                request.locationAccuracy());
        return IncidentResponse.from(incidentRepository.save(incident));
    }

    @Transactional(readOnly = true)
    public List<IncidentResponse> findAllForCitizen(UUID citizenId) {
        return incidentRepository.findByCitizenIdOrderByCreatedAtDesc(citizenId).stream()
                .map(IncidentResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public IncidentResponse findForCitizen(UUID citizenId, UUID incidentId) {
        return incidentRepository.findByIdAndCitizenId(incidentId, citizenId)
                .map(IncidentResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Incident not found"));
    }
}
