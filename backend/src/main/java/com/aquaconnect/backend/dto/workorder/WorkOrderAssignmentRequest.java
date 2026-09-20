package com.aquaconnect.backend.dto.workorder;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record WorkOrderAssignmentRequest(@NotNull UUID assignedEngineerId) {
}
