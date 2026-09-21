package com.aquaconnect.backend.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import java.util.UUID;

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
import com.aquaconnect.backend.enums.IvrLanguage;
import com.aquaconnect.backend.enums.IvrSessionState;
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
        when(repository.save(session)).thenReturn(session);
        when(session.getId()).thenReturn(sessionId);
        when(session.getState()).thenReturn(IvrSessionState.START);
    }

    @Test
    void languageSelectionMovesSessionToMainMenu() {
        service.selectLanguage(sessionId, new IvrLanguageRequest(IvrLanguage.ENGLISH));
        verify(session).selectLanguage(IvrLanguage.ENGLISH);
    }

    @Test
    void menuOneSelectsReportProblem() {
        when(session.getState()).thenReturn(IvrSessionState.MAIN_MENU);
        service.menu(sessionId, 1);
        verify(session).chooseReport();
    }

    @Test
    void menuFourEscalatesToOperator() {
        when(session.getState()).thenReturn(IvrSessionState.MAIN_MENU);
        service.menu(sessionId, 4);
        verify(session).chooseOperator();
    }

    @Test
    void menuNineRepeatsMenu() {
        when(session.getState()).thenReturn(IvrSessionState.MAIN_MENU);
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
        service.details(sessionId, new IvrDetailsRequest(IncidentCategory.PIPE_LEAK, "Leak near station"));
        verify(session).details(IncidentCategory.PIPE_LEAK, "Leak near station");
    }
}
