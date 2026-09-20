package com.aquaconnect.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.aquaconnect.backend.dto.incident.IncidentCorrelationCandidate;
import com.aquaconnect.backend.entity.Incident;
import com.aquaconnect.backend.enums.IncidentCategory;
import com.aquaconnect.backend.enums.IncidentPriority;
import com.aquaconnect.backend.enums.IncidentSource;
import com.aquaconnect.backend.enums.IncidentStatus;
import com.aquaconnect.backend.enums.LocationSource;
import com.aquaconnect.backend.repository.IncidentRepository;

@ExtendWith(MockitoExtension.class)
class IncidentCorrelationServiceTest {

    @Mock
    private IncidentRepository incidentRepository;

    @Test
    void identifiesRecentSameCategoryIncidentWithinOneKilometre() throws Exception {
        UUID targetId = UUID.randomUUID();
        Incident target = incident(targetId, IncidentCategory.PIPE_LEAK, "12.9716", "77.5946", Instant.now());
        Incident candidate = incident(UUID.randomUUID(), IncidentCategory.PIPE_LEAK, "12.9720", "77.5948", Instant.now());
        when(incidentRepository.findById(targetId)).thenReturn(java.util.Optional.of(target));
        when(incidentRepository.findAll()).thenReturn(List.of(target, candidate));

        List<IncidentCorrelationCandidate> results = new IncidentCorrelationService(incidentRepository)
                .findPotentiallyRelated(targetId);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).distanceMeters()).isLessThan(new BigDecimal("1000"));
        assertThat(results.get(0).reason()).contains("Same category");
        assertThat(target.getStatus()).isEqualTo(IncidentStatus.SUBMITTED);
        assertThat(target.getPriority()).isEqualTo(IncidentPriority.LOW);
    }

    @Test
    void excludesDifferentCategoryFarAndOldIncidents() throws Exception {
        UUID targetId = UUID.randomUUID();
        Incident target = incident(targetId, IncidentCategory.PIPE_LEAK, "12.9716", "77.5946", Instant.now());
        Incident different = incident(UUID.randomUUID(), IncidentCategory.PIPE_BURST, "12.9716", "77.5946", Instant.now());
        Incident far = incident(UUID.randomUUID(), IncidentCategory.PIPE_LEAK, "13.5000", "77.5946", Instant.now());
        Incident old = incident(UUID.randomUUID(), IncidentCategory.PIPE_LEAK, "12.9716", "77.5946", Instant.now().minusSeconds(90_000));
        when(incidentRepository.findById(targetId)).thenReturn(java.util.Optional.of(target));
        when(incidentRepository.findAll()).thenReturn(List.of(target, different, far, old));

        assertThat(new IncidentCorrelationService(incidentRepository).findPotentiallyRelated(targetId)).isEmpty();
    }

    private static Incident incident(UUID id, IncidentCategory category, String latitude, String longitude, Instant reportedAt)
            throws Exception {
        Incident incident = new Incident(null, IncidentSource.WEB, category, "Description",
                new BigDecimal(latitude), new BigDecimal(longitude), LocationSource.GPS, null);
        set(incident, "id", id);
        set(incident, "reportedAt", reportedAt);
        return incident;
    }

    private static void set(Object target, String name, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }
}
