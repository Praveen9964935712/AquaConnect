package com.aquaconnect.backend.service;

import java.time.Instant;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aquaconnect.backend.dto.workorder.WorkOrderResponse;
import com.aquaconnect.backend.entity.Incident;
import com.aquaconnect.backend.entity.User;
import com.aquaconnect.backend.entity.WorkOrder;
import com.aquaconnect.backend.enums.IncidentStatus;
import com.aquaconnect.backend.enums.RoleName;
import com.aquaconnect.backend.enums.WorkOrderStatus;
import com.aquaconnect.backend.exception.InvalidWorkOrderException;
import com.aquaconnect.backend.exception.InvalidWorkOrderTransitionException;
import com.aquaconnect.backend.exception.ResourceNotFoundException;
import com.aquaconnect.backend.repository.IncidentRepository;
import com.aquaconnect.backend.repository.UserRepository;
import com.aquaconnect.backend.repository.WorkOrderRepository;

@Service
public class WorkOrderService {

    private static final Map<WorkOrderStatus, Set<WorkOrderStatus>> ALLOWED_TRANSITIONS = transitions();

    private final WorkOrderRepository workOrderRepository;
    private final IncidentRepository incidentRepository;
    private final UserRepository userRepository;
    private final IncidentLifecycleService incidentLifecycleService;

    public WorkOrderService(WorkOrderRepository workOrderRepository,
            IncidentRepository incidentRepository, UserRepository userRepository,
            IncidentLifecycleService incidentLifecycleService) {
        this.workOrderRepository = workOrderRepository;
        this.incidentRepository = incidentRepository;
        this.userRepository = userRepository;
        this.incidentLifecycleService = incidentLifecycleService;
    }

    @Transactional
    public WorkOrderResponse create(UUID incidentId) {
        Incident incident = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new ResourceNotFoundException("Incident not found"));
        if (incident.getStatus() != IncidentStatus.VERIFIED) {
            throw new InvalidWorkOrderException("Work orders require a VERIFIED incident");
        }
        if (workOrderRepository.findByIncidentId(incidentId).isPresent()) {
            throw new InvalidWorkOrderException("An active work order already exists for this incident");
        }
        try {
            return WorkOrderResponse.from(workOrderRepository.saveAndFlush(new WorkOrder(incident, incident.getPriority())));
        } catch (DataIntegrityViolationException exception) {
            throw new InvalidWorkOrderException("An active work order already exists for this incident");
        }
    }

    @Transactional
    public WorkOrderResponse assign(UUID workOrderId, UUID engineerId) {
        WorkOrder workOrder = get(workOrderId);
        if (workOrder.getStatus() != WorkOrderStatus.CREATED) {
            throw new InvalidWorkOrderException("Only CREATED work orders can be assigned");
        }
        User engineer = userRepository.findById(engineerId)
                .orElseThrow(() -> new ResourceNotFoundException("Field engineer not found"));
        if (!hasRole(engineer, RoleName.FIELD_ENGINEER)) {
            throw new InvalidWorkOrderException("Assigned user must have the FIELD_ENGINEER role");
        }
        workOrder.assign(engineer);
        incidentLifecycleService.transition(workOrder.getIncident().getId(), IncidentStatus.ASSIGNED);
        return WorkOrderResponse.from(workOrderRepository.save(workOrder));
    }

    @Transactional
    public WorkOrderResponse transition(UUID workOrderId, WorkOrderStatus target, Authentication authentication) {
        WorkOrder workOrder = get(workOrderId);
        assertCanManage(workOrder, authentication);
        WorkOrderStatus current = workOrder.getStatus();
        if (!ALLOWED_TRANSITIONS.getOrDefault(current, Set.of()).contains(target)) {
            throw new InvalidWorkOrderTransitionException(current, target);
        }
        workOrder.setStatus(target);
        if (target == WorkOrderStatus.IN_PROGRESS) {
            incidentLifecycleService.transition(workOrder.getIncident().getId(), IncidentStatus.IN_PROGRESS);
        }
        return WorkOrderResponse.from(workOrderRepository.save(workOrder));
    }

    @Transactional
    public WorkOrderResponse accept(UUID workOrderId, Authentication authentication) {
        return transition(workOrderId, WorkOrderStatus.ACCEPTED, authentication);
    }

    @Transactional
    public WorkOrderResponse start(UUID workOrderId, Authentication authentication) {
        return transition(workOrderId, WorkOrderStatus.IN_PROGRESS, authentication);
    }

    @Transactional
    public WorkOrderResponse complete(UUID workOrderId, com.aquaconnect.backend.dto.workorder.WorkOrderFindingsRequest findings,
            Authentication authentication) {
        WorkOrder workOrder = get(workOrderId);
        assertCanManage(workOrder, authentication);
        workOrder.recordFindings(findings.inspectionNotes(), findings.observedCondition(), findings.repairNotes());
        if (!ALLOWED_TRANSITIONS.getOrDefault(workOrder.getStatus(), Set.of()).contains(WorkOrderStatus.COMPLETED)) {
            throw new InvalidWorkOrderTransitionException(workOrder.getStatus(), WorkOrderStatus.COMPLETED);
        }
        workOrder.setStatus(WorkOrderStatus.COMPLETED);
        incidentLifecycleService.transition(workOrder.getIncident().getId(), IncidentStatus.REPAIR_COMPLETED);
        incidentLifecycleService.transition(workOrder.getIncident().getId(), IncidentStatus.AUTHORITY_VERIFICATION);
        return WorkOrderResponse.from(workOrderRepository.save(workOrder));
    }

    @Transactional
    public WorkOrderResponse returnForRework(UUID workOrderId) {
        WorkOrder workOrder = get(workOrderId);
        if (workOrder.getStatus() != WorkOrderStatus.COMPLETED) {
            throw new InvalidWorkOrderException("Only completed work orders can return for rework");
        }
        workOrder.setStatus(WorkOrderStatus.IN_PROGRESS);
        return WorkOrderResponse.from(workOrderRepository.save(workOrder));
    }

    @Transactional(readOnly = true)
    public WorkOrderResponse findForActor(UUID workOrderId, Authentication authentication) {
        WorkOrder workOrder = get(workOrderId);
        assertCanView(workOrder, authentication);
        return WorkOrderResponse.from(workOrder);
    }

    @Transactional(readOnly = true)
    public WorkOrder getAuthorizedWorkOrder(UUID workOrderId, Authentication authentication) {
        WorkOrder workOrder = get(workOrderId);
        assertCanView(workOrder, authentication);
        return workOrder;
    }

    @Transactional(readOnly = true)
    public List<WorkOrderResponse> findAllForActor(Authentication authentication) {
        List<WorkOrder> workOrders = isOperational(authentication)
                ? workOrderRepository.findAllByOrderByCreatedAtDesc()
                : workOrderRepository.findByAssignedEngineerIdOrderByCreatedAtDesc(userId(authentication));
        return workOrders.stream().map(WorkOrderResponse::from).toList();
    }

    private WorkOrder get(UUID workOrderId) {
        return workOrderRepository.findById(workOrderId)
                .orElseThrow(() -> new ResourceNotFoundException("Work order not found"));
    }

    private void assertCanView(WorkOrder workOrder, Authentication authentication) {
        if (!isOperational(authentication)
                && (workOrder.getAssignedEngineer() == null
                || !userId(authentication).equals(workOrder.getAssignedEngineer().getId()))) {
            throw new ResourceNotFoundException("Work order not found");
        }
    }

    private void assertCanManage(WorkOrder workOrder, Authentication authentication) {
        if (!isOperational(authentication)
                && (workOrder.getAssignedEngineer() == null
                || !userId(authentication).equals(workOrder.getAssignedEngineer().getId()))) {
            throw new ResourceNotFoundException("Work order not found");
        }
    }

    private boolean isOperational(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(authority -> authority.getAuthority())
                .anyMatch(authority -> authority.equals("ROLE_OPERATOR")
                        || authority.equals("ROLE_OPERATIONS_MANAGER")
                        || authority.equals("ROLE_ADMIN"));
    }

    private UUID userId(Authentication authentication) {
        return UUID.fromString(authentication.getName());
    }

    private boolean hasRole(User user, RoleName roleName) {
        return user.getUserRoles().stream().anyMatch(userRole -> userRole.getRole().getName() == roleName);
    }

    private static Map<WorkOrderStatus, Set<WorkOrderStatus>> transitions() {
        Map<WorkOrderStatus, Set<WorkOrderStatus>> transitions = new EnumMap<>(WorkOrderStatus.class);
        transitions.put(WorkOrderStatus.CREATED, EnumSet.of(WorkOrderStatus.CANCELLED));
        transitions.put(WorkOrderStatus.ASSIGNED, EnumSet.of(WorkOrderStatus.ACCEPTED, WorkOrderStatus.CANCELLED));
        transitions.put(WorkOrderStatus.ACCEPTED, EnumSet.of(WorkOrderStatus.IN_PROGRESS, WorkOrderStatus.CANCELLED));
        transitions.put(WorkOrderStatus.IN_PROGRESS, EnumSet.of(WorkOrderStatus.COMPLETED, WorkOrderStatus.CANCELLED));
        return transitions;
    }
}
