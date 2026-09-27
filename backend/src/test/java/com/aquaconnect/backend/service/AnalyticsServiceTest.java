package com.aquaconnect.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.aquaconnect.backend.dto.analytics.DailyIncidentTrendPoint;
import com.aquaconnect.backend.entity.Incident;
import com.aquaconnect.backend.entity.Role;
import com.aquaconnect.backend.entity.User;
import com.aquaconnect.backend.entity.UserRole;
import com.aquaconnect.backend.enums.IncidentCategory;
import com.aquaconnect.backend.enums.IncidentPriority;
import com.aquaconnect.backend.enums.IncidentSource;
import com.aquaconnect.backend.enums.IncidentStatus;
import com.aquaconnect.backend.enums.RoleName;
import com.aquaconnect.backend.enums.VerificationDecision;
import com.aquaconnect.backend.enums.WorkOrderStatus;
import com.aquaconnect.backend.repository.AuthorityVerificationRepository;
import com.aquaconnect.backend.repository.IncidentRepository;
import com.aquaconnect.backend.repository.ResolutionConfirmationRepository;
import com.aquaconnect.backend.repository.UserRepository;
import com.aquaconnect.backend.repository.WorkOrderRepository;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {
    @Mock private IncidentRepository incidentRepository;
    @Mock private WorkOrderRepository workOrderRepository;
    @Mock private AuthorityVerificationRepository authorityVerificationRepository;
    @Mock private ResolutionConfirmationRepository resolutionConfirmationRepository;
    @Mock private UserRepository userRepository;
    private AnalyticsService service;

    @BeforeEach
    void setUp() {
        service = new AnalyticsService(incidentRepository, workOrderRepository,
                authorityVerificationRepository, resolutionConfirmationRepository, userRepository);
    }

    @Test
    void emptyDataReturnsZeroesAndEmptyTrend() {
        when(incidentRepository.count()).thenReturn(0L);
        when(incidentRepository.countByStatus(IncidentStatus.SUBMITTED)).thenReturn(0L);
        when(incidentRepository.countByStatus(IncidentStatus.UNDER_VERIFICATION)).thenReturn(0L);
        when(incidentRepository.countByStatus(IncidentStatus.VERIFIED)).thenReturn(0L);
        when(incidentRepository.countByStatus(IncidentStatus.ASSIGNED)).thenReturn(0L);
        when(incidentRepository.countByStatus(IncidentStatus.IN_PROGRESS)).thenReturn(0L);
        when(incidentRepository.countByStatus(IncidentStatus.REPAIR_COMPLETED)).thenReturn(0L);
        when(incidentRepository.countByStatus(IncidentStatus.AUTHORITY_VERIFICATION)).thenReturn(0L);
        when(incidentRepository.countByStatus(IncidentStatus.RESOLVED)).thenReturn(0L);
        when(incidentRepository.countByStatus(IncidentStatus.CLOSED)).thenReturn(0L);
        when(incidentRepository.countByStatus(IncidentStatus.REOPENED)).thenReturn(0L);
        when(incidentRepository.countByPriority(IncidentPriority.LOW)).thenReturn(0L);
        when(incidentRepository.countByPriority(IncidentPriority.MEDIUM)).thenReturn(0L);
        when(incidentRepository.countByPriority(IncidentPriority.HIGH)).thenReturn(0L);
        when(incidentRepository.countByPriority(IncidentPriority.CRITICAL)).thenReturn(0L);
        when(incidentRepository.countByCategory(IncidentCategory.PIPE_LEAK)).thenReturn(0L);
        when(incidentRepository.countByCategory(IncidentCategory.NO_WATER_SUPPLY)).thenReturn(0L);
        when(incidentRepository.countByCategory(IncidentCategory.LOW_WATER_PRESSURE)).thenReturn(0L);
        when(incidentRepository.countByCategory(IncidentCategory.PIPE_BURST)).thenReturn(0L);
        when(incidentRepository.countByCategory(IncidentCategory.CONTAMINATED_WATER)).thenReturn(0L);
        when(incidentRepository.countByCategory(IncidentCategory.VALVE_ISSUE)).thenReturn(0L);
        when(incidentRepository.countByCategory(IncidentCategory.OTHER)).thenReturn(0L);
        when(incidentRepository.countBySource(IncidentSource.WEB)).thenReturn(0L);
        when(incidentRepository.countBySource(IncidentSource.IVR)).thenReturn(0L);
        when(workOrderRepository.countByStatus(WorkOrderStatus.CREATED)).thenReturn(0L);
        when(workOrderRepository.countByStatus(WorkOrderStatus.ASSIGNED)).thenReturn(0L);
        when(workOrderRepository.countByStatus(WorkOrderStatus.ACCEPTED)).thenReturn(0L);
        when(workOrderRepository.countByStatus(WorkOrderStatus.IN_PROGRESS)).thenReturn(0L);
        when(workOrderRepository.countByStatus(WorkOrderStatus.COMPLETED)).thenReturn(0L);
        when(workOrderRepository.countByStatus(WorkOrderStatus.CANCELLED)).thenReturn(0L);
        when(authorityVerificationRepository.countByDecision(VerificationDecision.APPROVED)).thenReturn(0L);
        when(authorityVerificationRepository.countByDecision(VerificationDecision.REJECTED)).thenReturn(0L);
        when(resolutionConfirmationRepository.countAllByDecision(com.aquaconnect.backend.enums.ResolutionDecision.CONFIRMED)).thenReturn(0L);
        when(resolutionConfirmationRepository.countAllByDecision(com.aquaconnect.backend.enums.ResolutionDecision.STILL_PRESENT)).thenReturn(0L);
        when(userRepository.findAll()).thenReturn(List.of());

        var result = service.overview();
        assertThat(result.totalIncidents()).isZero();
        assertThat(result.openIncidents()).isZero();
        assertThat(result.incidentsByStatus()).containsEntry("CLOSED", 0L);
        assertThat(result.resolutionSummary().approvedResolutions()).isZero();
        assertThat(result.engineerWorkload()).isEmpty();
        assertThat(service.dailyIncidentTrend()).isEmpty();
    }

    @Test
    void aggregatesRepresentativeOperationalData() {
        when(incidentRepository.count()).thenReturn(8L);
        when(incidentRepository.countByStatus(IncidentStatus.SUBMITTED)).thenReturn(2L);
        when(incidentRepository.countByStatus(IncidentStatus.UNDER_VERIFICATION)).thenReturn(1L);
        when(incidentRepository.countByStatus(IncidentStatus.VERIFIED)).thenReturn(1L);
        when(incidentRepository.countByStatus(IncidentStatus.ASSIGNED)).thenReturn(1L);
        when(incidentRepository.countByStatus(IncidentStatus.IN_PROGRESS)).thenReturn(1L);
        when(incidentRepository.countByStatus(IncidentStatus.REPAIR_COMPLETED)).thenReturn(0L);
        when(incidentRepository.countByStatus(IncidentStatus.AUTHORITY_VERIFICATION)).thenReturn(0L);
        when(incidentRepository.countByStatus(IncidentStatus.RESOLVED)).thenReturn(1L);
        when(incidentRepository.countByStatus(IncidentStatus.CLOSED)).thenReturn(1L);
        when(incidentRepository.countByStatus(IncidentStatus.REOPENED)).thenReturn(0L);
        when(incidentRepository.countByPriority(IncidentPriority.LOW)).thenReturn(2L);
        when(incidentRepository.countByPriority(IncidentPriority.MEDIUM)).thenReturn(3L);
        when(incidentRepository.countByPriority(IncidentPriority.HIGH)).thenReturn(2L);
        when(incidentRepository.countByPriority(IncidentPriority.CRITICAL)).thenReturn(1L);
        when(incidentRepository.countByCategory(IncidentCategory.PIPE_LEAK)).thenReturn(1L);
        when(incidentRepository.countByCategory(IncidentCategory.NO_WATER_SUPPLY)).thenReturn(1L);
        when(incidentRepository.countByCategory(IncidentCategory.LOW_WATER_PRESSURE)).thenReturn(1L);
        when(incidentRepository.countByCategory(IncidentCategory.PIPE_BURST)).thenReturn(1L);
        when(incidentRepository.countByCategory(IncidentCategory.CONTAMINATED_WATER)).thenReturn(1L);
        when(incidentRepository.countByCategory(IncidentCategory.VALVE_ISSUE)).thenReturn(1L);
        when(incidentRepository.countByCategory(IncidentCategory.OTHER)).thenReturn(0L);
        when(incidentRepository.countBySource(IncidentSource.WEB)).thenReturn(6L);
        when(incidentRepository.countBySource(IncidentSource.IVR)).thenReturn(2L);
        when(workOrderRepository.countByStatus(WorkOrderStatus.CREATED)).thenReturn(1L);
        when(workOrderRepository.countByStatus(WorkOrderStatus.ASSIGNED)).thenReturn(2L);
        when(workOrderRepository.countByStatus(WorkOrderStatus.ACCEPTED)).thenReturn(1L);
        when(workOrderRepository.countByStatus(WorkOrderStatus.IN_PROGRESS)).thenReturn(1L);
        when(workOrderRepository.countByStatus(WorkOrderStatus.COMPLETED)).thenReturn(3L);
        when(workOrderRepository.countByStatus(WorkOrderStatus.CANCELLED)).thenReturn(0L);
        when(authorityVerificationRepository.countByDecision(VerificationDecision.APPROVED)).thenReturn(3L);
        when(authorityVerificationRepository.countByDecision(VerificationDecision.REJECTED)).thenReturn(1L);
        when(resolutionConfirmationRepository.countAllByDecision(com.aquaconnect.backend.enums.ResolutionDecision.CONFIRMED)).thenReturn(2L);
        when(resolutionConfirmationRepository.countAllByDecision(com.aquaconnect.backend.enums.ResolutionDecision.STILL_PRESENT)).thenReturn(1L);
        when(userRepository.findAll()).thenReturn(List.of(engineer("eng-1"), engineer("eng-2")));
        when(incidentRepository.findAllByCreatedAtAfterOrderByCreatedAtAsc(any(Instant.class))).thenReturn(List.of(recentIncident()));

        var result = service.overview();
        assertThat(result.totalIncidents()).isEqualTo(8L);
        assertThat(result.submittedIncidents()).isEqualTo(2L);
        assertThat(result.verifiedIncidents()).isEqualTo(1L);
        assertThat(result.assignedIncidents()).isEqualTo(1L);
        assertThat(result.inProgressIncidents()).isEqualTo(1L);
        assertThat(result.resolvedIncidents()).isEqualTo(1L);
        assertThat(result.closedIncidents()).isEqualTo(1L);
        assertThat(result.reopenedIncidents()).isZero();
        assertThat(result.incidentsBySource()).containsEntry("WEB", 6L);
        assertThat(result.workOrdersByStatus()).containsEntry("COMPLETED", 3L);
        assertThat(result.resolutionSummary().approvedResolutions()).isEqualTo(3L);
        assertThat(result.engineerWorkload()).hasSize(2);
        assertThat(result.dailyIncidentTrend()).isNotEmpty();
    }

    @Test
    void returnsDailyIncidentTrendForRecentWindow() {
        when(incidentRepository.findAllByCreatedAtAfterOrderByCreatedAtAsc(any(Instant.class))).thenReturn(List.of());
        var trend = service.dailyIncidentTrend();
        assertThat(trend).isNotNull();
        assertThat(trend).isEmpty();
    }

    private User engineer(String username) {
        User user = new User(username, username + "@example.com", "hash");
        Role role = new Role(RoleName.FIELD_ENGINEER, "Field engineer");
        UserRole userRole = new UserRole(user, role);
        user.getUserRoles().add(userRole);
        return user;
    }

    private Incident recentIncident() {
        User citizen = new User("citizen1", "citizen1@example.com", "hash");
        Incident incident = new Incident(citizen, IncidentSource.WEB, IncidentCategory.PIPE_LEAK,
                "Leak in pipeline", BigDecimal.valueOf(12.34), BigDecimal.valueOf(77.89),
                com.aquaconnect.backend.enums.LocationSource.GPS, BigDecimal.valueOf(10.0));
        ReflectionTestUtils.setField(incident, "createdAt", Instant.now());
        return incident;
    }
}
