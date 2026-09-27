package com.aquaconnect.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.web.server.ResponseStatusException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.aquaconnect.backend.dto.incident.IncidentResponse;
import com.aquaconnect.backend.dto.ivr.IvrDetailsRequest;
import com.aquaconnect.backend.dto.ivr.IvrLanguageRequest;
import com.aquaconnect.backend.dto.ivr.IvrMenuChoiceRequest;
import com.aquaconnect.backend.entity.IvrSession;
import com.aquaconnect.backend.enums.IncidentCategory;
import com.aquaconnect.backend.enums.IncidentSource;
import com.aquaconnect.backend.enums.IvrLanguage;
import com.aquaconnect.backend.enums.IvrSessionState;
import com.aquaconnect.backend.enums.LocationSource;
import com.aquaconnect.backend.repository.IvrSessionRepository;

@ExtendWith(MockitoExtension.class)
class IvrSessionServiceTest {
    @Mock private IvrSessionRepository repository;
    @Mock private IncidentService incidentService;
    private IvrSessionService service;
    private UUID sessionId;
    private IvrSession session;

    @BeforeEach
    void setUp() {
        service = new IvrSessionService(repository, incidentService);
        sessionId = UUID.randomUUID();
        session = mock(IvrSession.class);
        when(repository.findById(sessionId)).thenReturn(java.util.Optional.of(session));
    }

    @Test
    void languageSelectionMovesSessionToMainMenu() {
        when(session.getState()).thenReturn(IvrSessionState.START);
        when(repository.save(session)).thenReturn(session);
        service.selectLanguage(sessionId, new IvrLanguageRequest(IvrLanguage.ENGLISH));
        verify(session).selectLanguage(IvrLanguage.ENGLISH);
    }

    @Test
    void menuOneSelectsReportProblem() {
        when(session.getState()).thenReturn(IvrSessionState.MAIN_MENU);
        when(repository.save(session)).thenReturn(session);
        service.menu(sessionId, 1);
        verify(session).chooseReport();
    }

    @Test
    void menuFourEscalatesToOperator() {
        when(session.getState()).thenReturn(IvrSessionState.MAIN_MENU);
        when(repository.save(session)).thenReturn(session);
        service.menu(sessionId, 4);
        verify(session).chooseOperator();
    }

    @Test
    void menuNineRepeatsMenu() {
        when(session.getState()).thenReturn(IvrSessionState.MAIN_MENU);
        when(repository.save(session)).thenReturn(session);
        service.menu(sessionId, 9);
        verify(session).repeatMenu();
    }

    @Test
    void invalidMenuChoiceIsRejected() {
        when(session.getState()).thenReturn(IvrSessionState.MAIN_MENU);
        assertThatThrownBy(() -> service.menu(sessionId, 8)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void detailsAreAcceptedOnlyAfterReportSelection() {
        when(session.getState()).thenReturn(IvrSessionState.REPORT_PROBLEM);
        when(repository.save(session)).thenReturn(session);
        service.details(sessionId, new IvrDetailsRequest(IncidentCategory.PIPE_LEAK, "Leak near station"));
        verify(session).details(IncidentCategory.PIPE_LEAK, "Leak near station");
    }

    @Test
    void confirmationCreatesIncidentAndStoresAssociation() {
        UUID incidentId = UUID.randomUUID();
        when(session.getState()).thenReturn(IvrSessionState.CONFIRMATION);
        when(session.getCallerIdentifier()).thenReturn("caller-a");
        when(session.getSelectedCategory()).thenReturn(IncidentCategory.PIPE_LEAK);
        when(session.getDescription()).thenReturn("Leak near station");
        when(session.getLatitude()).thenReturn(BigDecimal.valueOf(12.9756));
        when(session.getLongitude()).thenReturn(BigDecimal.valueOf(77.5946));
        when(session.getLocationSource()).thenReturn(LocationSource.GPS);
        when(session.getLocationAccuracy()).thenReturn(BigDecimal.valueOf(12.5));
        when(incidentService.createForIvr(eq("caller-a"), any())).thenReturn(
                new IncidentResponse(incidentId, IncidentCategory.PIPE_LEAK, "Leak near station", IncidentSource.IVR,
                        BigDecimal.valueOf(12.9756), BigDecimal.valueOf(77.5946), LocationSource.GPS,
                        BigDecimal.valueOf(12.5), Instant.now(), null, null, Instant.now(), Instant.now()));
        when(repository.save(session)).thenReturn(session);

        service.confirm(sessionId, true);

        verify(incidentService).createForIvr(eq("caller-a"), any());
        verify(session).markIncidentCreated(incidentId);
        verify(session).complete();
    }

    @Test
    void cancelledConfirmationDoesNotCreateIncident() {
        when(session.getState()).thenReturn(IvrSessionState.CONFIRMATION);
        when(repository.save(session)).thenReturn(session);

        service.confirm(sessionId, false);

        verify(incidentService, never()).createForIvr(anyString(), any());
        verify(session).fail();
    }

    @Test
    void duplicateConfirmationIsRejected() {
        when(session.getState()).thenReturn(IvrSessionState.COMPLETED);

        assertThatThrownBy(() -> service.confirm(sessionId, true))
                .isInstanceOf(IllegalArgumentException.class);
        verify(incidentService, never()).createForIvr(anyString(), any());
    }

    @Test
    void missingIncidentDetailsIsRejectedBeforeCreation() {
        when(session.getState()).thenReturn(IvrSessionState.CONFIRMATION);
        when(session.getSelectedCategory()).thenReturn(IncidentCategory.PIPE_LEAK);
        when(session.getDescription()).thenReturn("   ");

        assertThatThrownBy(() -> service.confirm(sessionId, true))
                .isInstanceOf(IllegalArgumentException.class);
        verify(incidentService, never()).createForIvr(anyString(), any());
    }

    @Test
    void zeroComplaintsReturnsDeterministicCitizenSafeResult() {
        when(session.getCallerIdentifier()).thenReturn("caller-a");
        when(session.getSessionStatus()).thenReturn(com.aquaconnect.backend.enums.IvrSessionStatus.ACTIVE);
        when(incidentService.findForIvrCaller("caller-a")).thenReturn(List.of());

        var result = service.complaintStatusesForCaller(sessionId, "caller-a");

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().reference()).isEqualTo("NO_COMPLAINTS");
        assertThat(result.getFirst().statusMessage()).contains("No complaints were found");
    }

    @Test
    void oneComplaintReturnsCitizenSafeStatus() {
        when(session.getCallerIdentifier()).thenReturn("caller-a");
        when(session.getSessionStatus()).thenReturn(com.aquaconnect.backend.enums.IvrSessionStatus.ACTIVE);
        when(incidentService.findForIvrCaller("caller-a")).thenReturn(List.of(
                new IncidentResponse(UUID.randomUUID(), IncidentCategory.PIPE_LEAK, "Leak in lane", IncidentSource.IVR,
                        BigDecimal.valueOf(12.0), BigDecimal.valueOf(77.0), LocationSource.GPS, BigDecimal.ONE,
                        Instant.parse("2026-09-25T10:00:00Z"), com.aquaconnect.backend.enums.IncidentStatus.SUBMITTED,
                        com.aquaconnect.backend.enums.IncidentPriority.LOW, Instant.now(), Instant.now())));

        var result = service.complaintStatusesForCaller(sessionId, "caller-a");

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().statusMessage()).isEqualTo("Your complaint has been received.");
        assertThat(result.getFirst().status()).isEqualTo("SUBMITTED");
        assertThat(result.getFirst().category()).isEqualTo("PIPE_LEAK");
    }

    @Test
    void multipleComplaintsReturnBoundedCitizenSafeList() {
        when(session.getCallerIdentifier()).thenReturn("caller-a");
        when(session.getSessionStatus()).thenReturn(com.aquaconnect.backend.enums.IvrSessionStatus.ACTIVE);
        when(incidentService.findForIvrCaller("caller-a")).thenReturn(List.of(
                new IncidentResponse(UUID.randomUUID(), IncidentCategory.NO_WATER_SUPPLY, "No water", IncidentSource.IVR,
                        null, null, LocationSource.UNKNOWN, null, Instant.now(), com.aquaconnect.backend.enums.IncidentStatus.ASSIGNED,
                        com.aquaconnect.backend.enums.IncidentPriority.MEDIUM, Instant.now(), Instant.now()),
                new IncidentResponse(UUID.randomUUID(), IncidentCategory.LOW_WATER_PRESSURE, "Low pressure", IncidentSource.IVR,
                        null, null, LocationSource.UNKNOWN, null, Instant.now(), com.aquaconnect.backend.enums.IncidentStatus.RESOLVED,
                        com.aquaconnect.backend.enums.IncidentPriority.LOW, Instant.now(), Instant.now())));

        var result = service.complaintStatusesForCaller(sessionId, "caller-a");

        assertThat(result).hasSize(2);
        assertThat(result.get(0).statusMessage()).isEqualTo("Your complaint has been assigned to the field team.");
        assertThat(result.get(1).statusMessage()).isEqualTo("Your complaint has been resolved.");
    }

    @Test
    void callerOwnershipIsRequiredForComplaintStatus() {
        when(session.getCallerIdentifier()).thenReturn("caller-a");

        assertThatThrownBy(() -> service.complaintStatusesForCaller(sessionId, "caller-b"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("does not own this session");
    }

    @Test
    void invalidSessionRejectsComplaintStatusLookup() {
        when(session.getCallerIdentifier()).thenReturn("caller-a");
        when(session.getSessionStatus()).thenReturn(com.aquaconnect.backend.enums.IvrSessionStatus.COMPLETED);

        assertThatThrownBy(() -> service.complaintStatusesForCaller(sessionId, "caller-a"))
                .isInstanceOf(ResponseStatusException.class);
    }
}
