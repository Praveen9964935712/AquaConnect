package com.aquaconnect.backend.service;

import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aquaconnect.backend.dto.confirmation.ResolutionConfirmationRequest;
import com.aquaconnect.backend.dto.confirmation.ResolutionConfirmationResponse;
import com.aquaconnect.backend.entity.Incident;
import com.aquaconnect.backend.entity.ResolutionConfirmation;
import com.aquaconnect.backend.entity.User;
import com.aquaconnect.backend.enums.IncidentStatus;
import com.aquaconnect.backend.enums.ResolutionDecision;
import com.aquaconnect.backend.exception.InvalidResolutionConfirmationException;
import com.aquaconnect.backend.exception.ResourceNotFoundException;
import com.aquaconnect.backend.repository.IncidentRepository;
import com.aquaconnect.backend.repository.ResolutionConfirmationRepository;
import com.aquaconnect.backend.repository.UserRepository;

@Service
public class ResolutionConfirmationService {

    private final IncidentRepository incidentRepository;
    private final UserRepository userRepository;
    private final ResolutionConfirmationRepository confirmationRepository;
    private final IncidentLifecycleService lifecycleService;

    public ResolutionConfirmationService(IncidentRepository incidentRepository, UserRepository userRepository,
            ResolutionConfirmationRepository confirmationRepository, IncidentLifecycleService lifecycleService) {
        this.incidentRepository = incidentRepository;
        this.userRepository = userRepository;
        this.confirmationRepository = confirmationRepository;
        this.lifecycleService = lifecycleService;
    }

    @Transactional
    public ResolutionConfirmationResponse confirm(UUID incidentId, ResolutionConfirmationRequest request,
            Authentication authentication) {
        UUID citizenId = UUID.fromString(authentication.getName());
        User citizen = userRepository.findById(citizenId)
                .orElseThrow(() -> new ResourceNotFoundException("Citizen not found"));
        Incident incident = incidentRepository.findByIdAndCitizenId(incidentId, citizenId)
                .orElseThrow(() -> new ResourceNotFoundException("Incident not found"));
        if (incident.getStatus() != IncidentStatus.RESOLVED) {
            throw new InvalidResolutionConfirmationException("Only resolved incidents can be confirmed");
        }
        if (request.decision() == ResolutionDecision.STILL_PRESENT
                && (request.comment() == null || request.comment().isBlank())) {
            throw new InvalidResolutionConfirmationException("A comment is required when the issue is still present");
        }
        if (request.decision() == ResolutionDecision.CONFIRMED) {
            lifecycleService.transition(incidentId, IncidentStatus.CLOSED);
        } else {
            lifecycleService.transition(incidentId, IncidentStatus.REOPENED);
            lifecycleService.transition(incidentId, IncidentStatus.UNDER_VERIFICATION);
        }
        return ResolutionConfirmationResponse.from(confirmationRepository.saveAndFlush(
                new ResolutionConfirmation(incident, citizen, request.decision(), request.comment())));
    }
}
