package com.aquaconnect.backend.dto.confirmation;

import com.aquaconnect.backend.enums.ResolutionDecision;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ResolutionConfirmationRequest(
        @NotNull ResolutionDecision decision,
        @Size(max = 2000) String comment) {
}
