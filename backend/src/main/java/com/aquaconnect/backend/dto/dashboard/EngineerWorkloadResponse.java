package com.aquaconnect.backend.dto.dashboard;

import java.util.UUID;

public record EngineerWorkloadResponse(
        UUID engineerId,
        String username,
        long assigned,
        long accepted,
        long inProgress,
        long completed) {
}
