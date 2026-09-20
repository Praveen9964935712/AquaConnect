package com.aquaconnect.backend.dto.confirmation;

import java.time.Instant;
import java.util.UUID;

import com.aquaconnect.backend.entity.ResolutionConfirmation;
import com.aquaconnect.backend.enums.ResolutionDecision;

public record ResolutionConfirmationResponse(UUID id, UUID incidentId, ResolutionDecision decision,
        String comment, Instant createdAt) {

    public static ResolutionConfirmationResponse from(ResolutionConfirmation confirmation) {
        return new ResolutionConfirmationResponse(confirmation.getId(), confirmation.getIncident().getId(),
                confirmation.getDecision(), confirmation.getComment(), confirmation.getCreatedAt());
    }
}
