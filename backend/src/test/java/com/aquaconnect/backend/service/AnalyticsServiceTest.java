package com.aquaconnect.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.aquaconnect.backend.repository.IncidentRepository;
import com.aquaconnect.backend.repository.WorkOrderRepository;
import com.aquaconnect.backend.enums.IncidentStatus;
import com.aquaconnect.backend.enums.WorkOrderStatus;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {
    @Mock private IncidentRepository incidentRepository;
    @Mock private WorkOrderRepository workOrderRepository;
    private AnalyticsService service;

    @BeforeEach
    void setUp() { service = new AnalyticsService(incidentRepository, workOrderRepository); }

    @Test
    void emptyDataReturnsZeroesAndEmptyAverage() {
        when(incidentRepository.countOpen(Set.of(IncidentStatus.CLOSED))).thenReturn(0L);
        var result = service.overview();
        assertThat(result.totalIncidents()).isZero();
        assertThat(result.openIncidents()).isZero();
        assertThat(result.averageCompletionSeconds()).isNull();
        assertThat(result.incidentsByStatus()).containsEntry("CLOSED", 0L);
    }

    @Test
    void aggregatesRepresentativeOperationalData() {
        when(incidentRepository.countByStatus(IncidentStatus.SUBMITTED)).thenReturn(2L);
        when(incidentRepository.countByStatus(IncidentStatus.UNDER_VERIFICATION)).thenReturn(1L);
        when(incidentRepository.countByStatus(IncidentStatus.VERIFIED)).thenReturn(1L);
        when(incidentRepository.countByStatus(IncidentStatus.ASSIGNED)).thenReturn(0L);
        when(incidentRepository.countByStatus(IncidentStatus.IN_PROGRESS)).thenReturn(0L);
        when(incidentRepository.countByStatus(IncidentStatus.REPAIR_COMPLETED)).thenReturn(0L);
        when(incidentRepository.countByStatus(IncidentStatus.AUTHORITY_VERIFICATION)).thenReturn(0L);
        when(incidentRepository.countByStatus(IncidentStatus.RESOLVED)).thenReturn(1L);
        when(incidentRepository.countByStatus(IncidentStatus.CLOSED)).thenReturn(3L);
        when(incidentRepository.countByStatus(IncidentStatus.REOPENED)).thenReturn(0L);
        when(incidentRepository.countOpen(Set.of(IncidentStatus.CLOSED))).thenReturn(3L);
        when(workOrderRepository.countByStatus(WorkOrderStatus.CREATED)).thenReturn(0L);
        when(workOrderRepository.countByStatus(WorkOrderStatus.ASSIGNED)).thenReturn(0L);
        when(workOrderRepository.countByStatus(WorkOrderStatus.ACCEPTED)).thenReturn(0L);
        when(workOrderRepository.countByStatus(WorkOrderStatus.IN_PROGRESS)).thenReturn(0L);
        when(workOrderRepository.countByStatus(WorkOrderStatus.COMPLETED)).thenReturn(4L);
        when(workOrderRepository.countByStatus(WorkOrderStatus.CANCELLED)).thenReturn(0L);
        when(workOrderRepository.averageCompletionSeconds()).thenReturn(3600.0);
        var result = service.overview();
        assertThat(result.totalIncidents()).isEqualTo(8L);
        assertThat(result.resolvedIncidents()).isEqualTo(1L);
        assertThat(result.closedIncidents()).isEqualTo(3L);
        assertThat(result.workOrdersByStatus()).containsEntry("COMPLETED", 4L);
        assertThat(result.averageCompletionSeconds()).isEqualTo(3600.0);
    }
}
