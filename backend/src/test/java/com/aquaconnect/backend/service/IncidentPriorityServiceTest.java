package com.aquaconnect.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.aquaconnect.backend.dto.incident.PriorityRecommendation;
import com.aquaconnect.backend.entity.Incident;
import com.aquaconnect.backend.enums.IncidentCategory;
import com.aquaconnect.backend.enums.IncidentPriority;
import com.aquaconnect.backend.enums.IncidentSource;
import com.aquaconnect.backend.enums.IncidentStatus;
import com.aquaconnect.backend.enums.LocationSource;
import com.aquaconnect.backend.repository.IncidentRepository;
import com.aquaconnect.backend.repository.SupportingReportRepository;

@ExtendWith(MockitoExtension.class)
class IncidentPriorityServiceTest {

    @Mock
    private IncidentRepository incidentRepository;

    @Mock
    private SupportingReportRepository supportingReportRepository;

    private IncidentPriorityService priorityService;
    private UUID incidentId;

    @BeforeEach
    void setUp() throws Exception {
        priorityService = new IncidentPriorityService(incidentRepository, supportingReportRepository);
        incidentId = UUID.randomUUID();
    }

    @Test
    void otherIncidentWithoutSignalsIsLow() throws Exception {
        Incident incident = incident(IncidentCategory.OTHER, Instant.now());
        when(incidentRepository.findById(incidentId)).thenReturn(Optional.of(incident));
        when(supportingReportRepository.countByPrimaryIncidentId(incidentId)).thenReturn(0L);

        PriorityRecommendation recommendation = priorityService.recommend(incidentId);

        assertThat(recommendation.priority()).isEqualTo(IncidentPriority.LOW);
        assertThat(recommendation.reason()).contains("No elevated severity");
        assertThat(incident.getStatus()).isEqualTo(IncidentStatus.SUBMITTED);
        assertThat(incident.getPriority()).isEqualTo(IncidentPriority.LOW);
    }

    @Test
    void highSeverityAndSupportingReportsRaisePriorityWithExplanation() throws Exception {
        Incident incident = incident(IncidentCategory.PIPE_BURST, Instant.now());
        when(incidentRepository.findById(incidentId)).thenReturn(Optional.of(incident));
        when(supportingReportRepository.countByPrimaryIncidentId(incidentId)).thenReturn(3L);

        PriorityRecommendation first = priorityService.recommend(incidentId);
        PriorityRecommendation second = priorityService.recommend(incidentId);

        assertThat(first).isEqualTo(second);
        assertThat(first.priority()).isEqualTo(IncidentPriority.CRITICAL);
        assertThat(first.reason()).contains("pipe burst").contains("3 supporting reports");
        verify(incidentRepository, org.mockito.Mockito.times(2)).findById(incidentId);
    }

    @Test
    void oldMediumIncidentGetsOneAgeIncrease() throws Exception {
        Incident incident = incident(IncidentCategory.PIPE_LEAK, Instant.now().minusSeconds(90_000));
        when(incidentRepository.findById(incidentId)).thenReturn(Optional.of(incident));
        when(supportingReportRepository.countByPrimaryIncidentId(incidentId)).thenReturn(0L);

        PriorityRecommendation recommendation = priorityService.recommend(incidentId);

        assertThat(recommendation.priority()).isEqualTo(IncidentPriority.HIGH);
        assertThat(recommendation.reason()).contains("24 hours");
    }

    private Incident incident(IncidentCategory category, Instant reportedAt) throws Exception {
        Incident incident = new Incident(null, IncidentSource.WEB, category, "Description", null, null,
                LocationSource.UNKNOWN, null);
        Field reportedAtField = Incident.class.getDeclaredField("reportedAt");
        reportedAtField.setAccessible(true);
        reportedAtField.set(incident, reportedAt);
        return incident;
    }
}
