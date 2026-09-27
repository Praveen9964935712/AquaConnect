package com.aquaconnect.backend.dto.analytics;

import java.util.UUID;

public record EngineerWorkloadSummary(
        UUID engineerId,
        String username,
        long assignedWorkOrders,
        long activeWorkOrders,
        long completedWorkOrders) {
}
