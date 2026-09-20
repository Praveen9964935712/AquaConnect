package com.aquaconnect.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.lenient;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.aquaconnect.backend.entity.Incident;
import com.aquaconnect.backend.enums.IncidentCategory;
import com.aquaconnect.backend.enums.IncidentSource;
import com.aquaconnect.backend.enums.IncidentStatus;
import com.aquaconnect.backend.enums.LocationSource;
import com.aquaconnect.backend.exception.InvalidIncidentTransitionException;
import com.aquaconnect.backend.repository.IncidentRepository;

@ExtendWith(MockitoExtension.class)
class IncidentLifecycleServiceTest {

    @Mock
    private IncidentRepository incidentRepository;

    private IncidentLifecycleService lifecycleService;
    private Incident incident;
    private UUID incidentId;

    @BeforeEach
    void setUp() {
        lifecycleService = new IncidentLifecycleService(incidentRepository);
        incident = new Incident(null, IncidentSource.WEB, IncidentCategory.PIPE_LEAK,
                "Leak near the main road", new BigDecimal("12.9"), new BigDecimal("77.5"),
                LocationSource.GPS, new BigDecimal("5"));
        incidentId = UUID.randomUUID();
        when(incidentRepository.findById(incidentId)).thenReturn(Optional.of(incident));
        lenient().when(incidentRepository.save(any(Incident.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void allowsEveryForwardTransition() {
        IncidentStatus[] statuses = {
                IncidentStatus.UNDER_VERIFICATION,
                IncidentStatus.VERIFIED,
                IncidentStatus.ASSIGNED,
                IncidentStatus.IN_PROGRESS,
                IncidentStatus.REPAIR_COMPLETED,
                IncidentStatus.AUTHORITY_VERIFICATION,
                IncidentStatus.RESOLVED,
                IncidentStatus.CLOSED
        };

        for (IncidentStatus status : statuses) {
            lifecycleService.transition(incidentId, status);
            assertThat(incident.getStatus()).isEqualTo(status);
        }
    }

    @Test
    void allowsResolvedIncidentToReopenAndReturnToVerification() {
        incident.setStatus(IncidentStatus.RESOLVED);

        lifecycleService.transition(incidentId, IncidentStatus.REOPENED);
        lifecycleService.transition(incidentId, IncidentStatus.UNDER_VERIFICATION);

        assertThat(incident.getStatus()).isEqualTo(IncidentStatus.UNDER_VERIFICATION);
    }

    @Test
    void rejectsSkippedAndClosedTransitions() {
        assertThatThrownBy(() -> lifecycleService.transition(incidentId, IncidentStatus.VERIFIED))
                .isInstanceOf(InvalidIncidentTransitionException.class);

        incident.setStatus(IncidentStatus.CLOSED);
        assertThatThrownBy(() -> lifecycleService.transition(incidentId, IncidentStatus.SUBMITTED))
                .isInstanceOf(InvalidIncidentTransitionException.class);
    }
}
