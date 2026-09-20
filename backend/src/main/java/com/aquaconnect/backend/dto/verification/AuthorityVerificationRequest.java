package com.aquaconnect.backend.dto.verification;

import com.aquaconnect.backend.enums.VerificationDecision;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AuthorityVerificationRequest(
        @NotNull VerificationDecision decision,
        @Size(max = 2000) String notes) {
}
