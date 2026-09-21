package com.aquaconnect.backend.service;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aquaconnect.backend.dto.analytics.AnalyticsResponse;
import com.aquaconnect.backend.enums.IncidentCategory;
import com.aquaconnect.backend.enums.IncidentPriority;
import com.aquaconnect.backend.enums.IncidentSource;
import com.aquaconnect.backend.enums.IncidentStatus;
import com.aquaconnect.backend.enums.WorkOrderStatus;
import com.aquaconnect.backend.repository.IncidentRepository;
import com.aquaconnect.backend.repository.WorkOrderRepository;

@Service
public class AnalyticsService {

    private final IncidentRepository incidentRepository;
    private final WorkOrderRepository workOrderRepository;

    public AnalyticsService(IncidentRepository incidentRepository, WorkOrderRepository workOrderRepository) {
        this.incidentRepository = incidentRepository;
        this.workOrderRepository = workOrderRepository;
    }

    @Transactional(readOnly = true)
    public AnalyticsResponse overview() {
        Map<String, Long> status = counts(IncidentStatus.values(), incidentRepository::countByStatus);
        Map<String, Long> priority = counts(IncidentPriority.values(), incidentRepository::countByPriority);
        Map<String, Long> category = counts(IncidentCategory.values(), incidentRepository::countByCategory);
        Map<String, Long> source = counts(IncidentSource.values(), incidentRepository::countBySource);
        Map<String, Long> workOrders = counts(WorkOrderStatus.values(), workOrderRepository::countByStatus);
        long total = status.values().stream().mapToLong(Long::longValue).sum();
        long resolved = status.getOrDefault(IncidentStatus.RESOLVED.name(), 0L);
        long closed = status.getOrDefault(IncidentStatus.CLOSED.name(), 0L);
        long reopened = status.getOrDefault(IncidentStatus.REOPENED.name(), 0L);
        long open = incidentRepository.countOpen(java.util.Set.of(IncidentStatus.CLOSED));
        return new AnalyticsResponse(total, open, resolved, closed, reopened, status, priority, category, source,
                workOrders, workOrderRepository.averageCompletionSeconds());
    }

    private <E extends Enum<E>> Map<String, Long> counts(E[] values, java.util.function.ToLongFunction<E> counter) {
        Map<String, Long> result = new java.util.LinkedHashMap<>();
        for (E value : values) result.put(value.name(), counter.applyAsLong(value));
        return result;
    }
}
