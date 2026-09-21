package com.aquaconnect.backend.service;

import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aquaconnect.backend.dto.dashboard.EngineerWorkloadResponse;
import com.aquaconnect.backend.dto.dashboard.ManagerDashboardResponse;
import com.aquaconnect.backend.dto.incident.IncidentResponse;
import com.aquaconnect.backend.dto.workorder.WorkOrderResponse;
import com.aquaconnect.backend.entity.User;
import com.aquaconnect.backend.entity.WorkOrder;
import com.aquaconnect.backend.enums.IncidentPriority;
import com.aquaconnect.backend.enums.IncidentStatus;
import com.aquaconnect.backend.enums.RoleName;
import com.aquaconnect.backend.enums.WorkOrderStatus;
import com.aquaconnect.backend.repository.IncidentRepository;
import com.aquaconnect.backend.repository.UserRepository;
import com.aquaconnect.backend.repository.WorkOrderRepository;

@Service
public class OperationalDashboardService {

    private final IncidentRepository incidentRepository;
    private final WorkOrderRepository workOrderRepository;
    private final UserRepository userRepository;

    public OperationalDashboardService(IncidentRepository incidentRepository,
            WorkOrderRepository workOrderRepository, UserRepository userRepository) {
        this.incidentRepository = incidentRepository;
        this.workOrderRepository = workOrderRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public ManagerDashboardResponse managerDashboard() {
        var incidents = incidentRepository.findAll();
        var workOrders = workOrderRepository.findAllByOrderByCreatedAtDesc();
        Map<IncidentStatus, Long> byStatus = enumCounts(IncidentStatus.values(), incidents.stream().map(i -> i.getStatus()).toList());
        Map<IncidentPriority, Long> byPriority = enumCounts(IncidentPriority.values(), incidents.stream().map(i -> i.getPriority()).toList());
        Map<WorkOrderStatus, Long> byWorkOrderStatus = enumCounts(WorkOrderStatus.values(), workOrders.stream().map(WorkOrder::getStatus).toList());
        List<EngineerWorkloadResponse> workloads = userRepository.findAll().stream()
                .filter(user -> hasRole(user, RoleName.FIELD_ENGINEER))
                .map(user -> workload(user, workOrders))
                .toList();
        List<IncidentResponse> priorityQueue = incidents.stream()
                .filter(incident -> incident.getStatus() != IncidentStatus.CLOSED)
                .sorted(Comparator.comparing((com.aquaconnect.backend.entity.Incident incident) -> incident.getPriority().ordinal()).reversed()
                        .thenComparing(com.aquaconnect.backend.entity.Incident::getCreatedAt))
                .map(IncidentResponse::from)
                .toList();
        return new ManagerDashboardResponse(byStatus, byPriority, byWorkOrderStatus,
                byStatus.getOrDefault(IncidentStatus.AUTHORITY_VERIFICATION, 0L),
                byStatus.getOrDefault(IncidentStatus.RESOLVED, 0L),
                byStatus.getOrDefault(IncidentStatus.CLOSED, 0L),
                byStatus.getOrDefault(IncidentStatus.REOPENED, 0L), workloads,
                priorityQueue, workOrders.stream().map(WorkOrderResponse::from).toList());
    }

    private EngineerWorkloadResponse workload(User user, List<WorkOrder> workOrders) {
        List<WorkOrder> assigned = workOrders.stream()
                .filter(order -> order.getAssignedEngineer() != null && order.getAssignedEngineer().getId().equals(user.getId()))
                .toList();
        return new EngineerWorkloadResponse(user.getId(), user.getUsername(), count(assigned, WorkOrderStatus.ASSIGNED),
                count(assigned, WorkOrderStatus.ACCEPTED), count(assigned, WorkOrderStatus.IN_PROGRESS), count(assigned, WorkOrderStatus.COMPLETED));
    }

    private long count(List<WorkOrder> workOrders, WorkOrderStatus status) {
        return workOrders.stream().filter(order -> order.getStatus() == status).count();
    }

    private boolean hasRole(User user, RoleName role) {
        return user.getUserRoles().stream().anyMatch(userRole -> userRole.getRole().getName() == role);
    }

    private <E extends Enum<E>> Map<E, Long> enumCounts(E[] values, List<E> actualValues) {
        Map<E, Long> result = new EnumMap<>(values[0].getDeclaringClass());
        for (E value : values) {
            result.put(value, actualValues.stream().filter(value::equals).count());
        }
        return result;
    }
}
