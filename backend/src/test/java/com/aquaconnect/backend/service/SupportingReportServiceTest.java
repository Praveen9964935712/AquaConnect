package com.aquaconnect.backend.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.aquaconnect.backend.entity.Incident;
import com.aquaconnect.backend.entity.User;
import com.aquaconnect.backend.enums.IncidentCategory;
import com.aquaconnect.backend.enums.IncidentSource;
import com.aquaconnect.backend.enums.LocationSource;
import com.aquaconnect.backend.exception.InvalidSupportingReportException;
import com.aquaconnect.backend.repository.IncidentRepository;
import com.aquaconnect.backend.repository.SupportingReportRepository;

@ExtendWith(MockitoExtension.class)
class SupportingReportServiceTest {

    @Mock
    private IncidentRepository incidentRepository;

    @Mock
    private SupportingReportRepository supportingReportRepository;

    private SupportingReportService service;
    private UUID citizenId;
    private UUID primaryId;
    private UUID supportingId;

    @BeforeEach
    void setUp() {
        service = new SupportingReportService(incidentRepository, supportingReportRepository);
        citizenId = UUID.randomUUID();
        primaryId = UUID.randomUUID();
        supportingId = UUID.randomUUID();
    }

    @Test
    void rejectsSelfSupport() {
        assertThatThrownBy(() -> service.addForCitizen(citizenId, primaryId, primaryId))
                .isInstanceOf(InvalidSupportingReportException.class);
    }

    @Test
    void rejectsDuplicatePair() {
        Incident primary = incident();
        Incident supporting = incident();
        when(incidentRepository.findById(primaryId)).thenReturn(Optional.of(primary));
        when(incidentRepository.findByIdAndCitizenId(supportingId, citizenId)).thenReturn(Optional.of(supporting));
        when(supportingReportRepository.existsByPrimaryIncidentIdAndSupportingIncidentId(primaryId, supportingId))
                .thenReturn(true);

        assertThatThrownBy(() -> service.addForCitizen(citizenId, primaryId, supportingId))
                .isInstanceOf(InvalidSupportingReportException.class);
    }

    private Incident incident() {
        return new Incident(new User("citizen", "citizen-" + UUID.randomUUID() + "@example.com", "hash"),
                IncidentSource.WEB, IncidentCategory.PIPE_LEAK, "Description", null, null, LocationSource.UNKNOWN, null);
    }
}
