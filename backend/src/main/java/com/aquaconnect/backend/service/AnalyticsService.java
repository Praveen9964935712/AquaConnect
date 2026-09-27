package com.aquaconnect.backend.service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.ToLongFunction;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aquaconnect.backend.dto.analytics.AnalyticsResponse;
import com.aquaconnect.backend.dto.analytics.DailyIncidentTrendPoint;
import com.aquaconnect.backend.dto.analytics.EngineerWorkloadSummary;
import com.aquaconnect.backend.dto.analytics.ResolutionSummary;
import com.aquaconnect.backend.entity.Role;
import com.aquaconnect.backend.entity.User;
import com.aquaconnect.backend.entity.WorkOrder;
import com.aquaconnect.backend.enums.IncidentCategory;
import com.aquaconnect.backend.enums.IncidentPriority;
import com.aquaconnect.backend.enums.IncidentSource;
import com.aquaconnect.backend.enums.IncidentStatus;
import com.aquaconnect.backend.enums.ResolutionDecision;
import com.aquaconnect.backend.enums.RoleName;
import com.aquaconnect.backend.enums.VerificationDecision;
import com.aquaconnect.backend.enums.WorkOrderStatus;
import com.aquaconnect.backend.repository.AuthorityVerificationRepository;
import com.aquaconnect.backend.repository.IncidentRepository;
import com.aquaconnect.backend.repository.ResolutionConfirmationRepository;
import com.aquaconnect.backend.repository.UserRepository;
import com.aquaconnect.backend.repository.WorkOrderRepository;

@Service
public class AnalyticsService {

    private final IncidentRepository incidentRepository;
    private final WorkOrderRepository workOrderRepository;
    private final AuthorityVerificationRepository authorityVerificationRepository;
    private final ResolutionConfirmationRepository resolutionConfirmationRepository;
    private final UserRepository userRepository;

    public AnalyticsService(IncidentRepository incidentRepository,
            WorkOrderRepository workOrderRepository,
            AuthorityVerificationRepository authorityVerificationRepository,
            ResolutionConfirmationRepository resolutionConfirmationRepository,
            UserRepository userRepository) {
        this.incidentRepository = incidentRepository;
        this.workOrderRepository = workOrderRepository;
        this.authorityVerificationRepository = authorityVerificationRepository;
        this.resolutionConfirmationRepository = resolutionConfirmationRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public AnalyticsResponse overview() {
        Map<String, Long> status = counts(IncidentStatus.values(), incidentRepository::countByStatus);
        Map<String, Long> priority = counts(IncidentPriority.values(), incidentRepository::countByPriority);
        Map<String, Long> category = counts(IncidentCategory.values(), incidentRepository::countByCategory);
        Map<String, Long> source = counts(IncidentSource.values(), incidentRepository::countBySource);
        Map<String, Long> workOrders = counts(WorkOrderStatus.values(), workOrderRepository::countByStatus);

        long total = incidentRepository.count();
        long open = incidentRepository.countOpen(Set.of(IncidentStatus.CLOSED));
        long submitted = status.getOrDefault(IncidentStatus.SUBMITTED.name(), 0L);
        long verified = status.getOrDefault(IncidentStatus.VERIFIED.name(), 0L);
        long assigned = status.getOrDefault(IncidentStatus.ASSIGNED.name(), 0L);
        long inProgress = status.getOrDefault(IncidentStatus.IN_PROGRESS.name(), 0L);
        long resolved = status.getOrDefault(IncidentStatus.RESOLVED.name(), 0L);
        long closed = status.getOrDefault(IncidentStatus.CLOSED.name(), 0L);
        long reopened = status.getOrDefault(IncidentStatus.REOPENED.name(), 0L);

        ResolutionSummary resolutionSummary = new ResolutionSummary(
                authorityVerificationRepository.countByDecision(VerificationDecision.APPROVED),
                authorityVerificationRepository.countByDecision(VerificationDecision.REJECTED),
                resolutionConfirmationRepository.countAllByDecision(ResolutionDecision.CONFIRMED),
                resolutionConfirmationRepository.countAllByDecision(ResolutionDecision.STILL_PRESENT));

        List<EngineerWorkloadSummary> workloadSummaries = engineerWorkload();
        List<DailyIncidentTrendPoint> trend = dailyIncidentTrend();
        Double averageCompletionSeconds = workOrderRepository.averageCompletionSeconds();
        if (averageCompletionSeconds != null && averageCompletionSeconds == 0.0d) {
            averageCompletionSeconds = null;
        }

        return new AnalyticsResponse(total, open, submitted, verified, assigned, inProgress,
                resolved, closed, reopened, status, priority, category, source, workOrders,
                resolutionSummary, workloadSummaries, trend, averageCompletionSeconds);
    }

    @Transactional(readOnly = true)
    public List<EngineerWorkloadSummary> engineerWorkload() {
        List<User> users = userRepository.findAll();
        if (users.isEmpty()) {
            return List.of();
        }
        List<WorkOrder> workOrders = workOrderRepository.findAll();
        List<EngineerWorkloadSummary> summaries = new ArrayList<>();
        for (User user : users) {
            if (!hasRole(user, RoleName.FIELD_ENGINEER)) {
                continue;
            }
            List<WorkOrder> assigned = workOrders.stream()
                    .filter(order -> order.getAssignedEngineer() != null && order.getAssignedEngineer().getId().equals(user.getId()))
                    .toList();
            long assignedCount = assigned.size();
            long activeCount = assigned.stream()
                    .filter(order -> order.getStatus() == WorkOrderStatus.ASSIGNED
                            || order.getStatus() == WorkOrderStatus.ACCEPTED
                            || order.getStatus() == WorkOrderStatus.IN_PROGRESS)
                    .count();
            long completedCount = assigned.stream()
                    .filter(order -> order.getStatus() == WorkOrderStatus.COMPLETED)
                    .count();
            summaries.add(new EngineerWorkloadSummary(user.getId(), user.getUsername(), assignedCount, activeCount, completedCount));
        }
        return summaries;
    }

    @Transactional(readOnly = true)
    public List<DailyIncidentTrendPoint> dailyIncidentTrend() {
        Instant cutoff = Instant.now().minusSeconds(30L * 24L * 60L * 60L);
        List<com.aquaconnect.backend.entity.Incident> incidents = incidentRepository.findAllByCreatedAtAfterOrderByCreatedAtAsc(cutoff);
        if (incidents == null || incidents.isEmpty()) {
            return List.of();
        }

        Map<LocalDate, Long> byDay = new LinkedHashMap<>();
        LocalDate today = LocalDate.now(ZoneId.systemDefault());
        for (int i = 29; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            byDay.put(date, 0L);
        }

        for (var incident : incidents) {
            LocalDate date = incident.getCreatedAt().atZone(ZoneId.systemDefault()).toLocalDate();
            if (date.isBefore(today.minusDays(29)) || date.isAfter(today)) {
                continue;
            }
            byDay.merge(date, 1L, Long::sum);
        }

        return byDay.entrySet().stream()
                .filter(entry -> entry.getValue() > 0L)
                .map(entry -> new DailyIncidentTrendPoint(entry.getKey(), entry.getValue()))
                .toList();
    }

    private boolean hasRole(User user, RoleName roleName) {
        if (user == null || user.getUserRoles() == null) {
            return false;
        }
        for (Role role : user.getUserRoles().stream().map(userRole -> userRole.getRole()).toList()) {
            if (role != null && role.getName() == roleName) {
                return true;
            }
        }
        return false;
    }

    private <E extends Enum<E>> Map<String, Long> counts(E[] values, ToLongFunction<E> counter) {
        Map<String, Long> result = new LinkedHashMap<>();
        for (E value : values) {
            result.put(value.name(), counter.applyAsLong(value));
        }
        return result;
    }
}
