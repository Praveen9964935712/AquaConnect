package com.aquaconnect.backend.dto.ivr;

import java.time.Instant;

public record IvrComplaintStatusResponse(
        String reference,
        String category,
        Instant submittedAt,
        String status,
        String statusMessage) {
}
