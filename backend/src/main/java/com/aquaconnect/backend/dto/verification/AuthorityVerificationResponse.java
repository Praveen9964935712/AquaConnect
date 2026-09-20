package com.aquaconnect.backend.dto.verification;

import java.time.Instant;
import java.util.UUID;

import com.aquaconnect.backend.entity.AuthorityVerification;
import com.aquaconnect.backend.enums.VerificationDecision;

public record AuthorityVerificationResponse(UUID id, UUID workOrderId, UUID incidentId,
        UUID verifiedByUserId, VerificationDecision decision, String notes, Instant createdAt) {

    public static AuthorityVerificationResponse from(AuthorityVerification verification) {
        return new AuthorityVerificationResponse(verification.getId(), verification.getWorkOrder().getId(),
                verification.getIncident().getId(), verification.getVerifiedBy().getId(), verification.getDecision(),
                verification.getNotes(), verification.getCreatedAt());
    }
}
