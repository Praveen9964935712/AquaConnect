package com.aquaconnect.backend.dto.workorder;

import java.time.Instant;
import java.util.UUID;

import com.aquaconnect.backend.entity.WorkOrder;
import com.aquaconnect.backend.enums.IncidentPriority;
import com.aquaconnect.backend.enums.WorkOrderStatus;

public record WorkOrderResponse(
        UUID id,
        UUID incidentId,
        UUID assignedEngineerId,
        WorkOrderStatus status,
        IncidentPriority priority,
        Instant createdAt,
        Instant updatedAt,
        Instant assignedAt,
        Instant startedAt,
        Instant completedAt,
        String inspectionNotes,
        String observedCondition,
        String repairNotes) {

    public static WorkOrderResponse from(WorkOrder workOrder) {
        return new WorkOrderResponse(
                workOrder.getId(),
                workOrder.getIncident().getId(),
                workOrder.getAssignedEngineer() == null ? null : workOrder.getAssignedEngineer().getId(),
                workOrder.getStatus(),
                workOrder.getPriority(),
                workOrder.getCreatedAt(),
                workOrder.getUpdatedAt(),
                workOrder.getAssignedAt(),
                workOrder.getStartedAt(),
                workOrder.getCompletedAt(),
                workOrder.getInspectionNotes(),
                workOrder.getObservedCondition(),
                workOrder.getRepairNotes());
    }
}
