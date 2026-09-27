package com.aquaconnect.backend.dto.analytics;

public record ResolutionSummary(
        long approvedResolutions,
        long rejectedResolutions,
        long citizenConfirmedClosures,
        long citizenStillPresentReopenedCases) {
}
