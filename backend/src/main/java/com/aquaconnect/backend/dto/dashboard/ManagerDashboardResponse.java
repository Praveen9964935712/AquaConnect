package com.aquaconnect.backend.dto.dashboard;

import java.util.List;
import java.util.Map;

import com.aquaconnect.backend.dto.incident.IncidentResponse;
import com.aquaconnect.backend.dto.workorder.WorkOrderResponse;
import com.aquaconnect.backend.enums.IncidentPriority;
import com.aquaconnect.backend.enums.IncidentStatus;
import com.aquaconnect.backend.enums.WorkOrderStatus;

public record ManagerDashboardResponse(
        Map<IncidentStatus, Long> incidentsByStatus,
        Map<IncidentPriority, Long> incidentsByPriority,
        Map<WorkOrderStatus, Long> workOrdersByStatus,
        long pendingAuthorityVerification,
        long resolvedIncidents,
        long closedIncidents,
        long reopenedIncidents,
        List<EngineerWorkloadResponse> engineerWorkloads,
        List<IncidentResponse> priorityQueue,
        List<WorkOrderResponse> workOrders) {
}
