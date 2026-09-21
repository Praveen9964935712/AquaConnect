package com.aquaconnect.backend.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aquaconnect.backend.dto.incident.IncidentCreateRequest;
import com.aquaconnect.backend.dto.incident.IncidentResponse;
import com.aquaconnect.backend.dto.ivr.IvrDetailsRequest;
import com.aquaconnect.backend.dto.ivr.IvrLanguageRequest;
import com.aquaconnect.backend.dto.ivr.IvrLocationRequest;
import com.aquaconnect.backend.dto.ivr.IvrSessionResponse;
import com.aquaconnect.backend.entity.IvrSession;
import com.aquaconnect.backend.enums.IncidentSource;
import com.aquaconnect.backend.enums.IvrSessionState;
import com.aquaconnect.backend.enums.LocationSource;
import com.aquaconnect.backend.exception.ResourceNotFoundException;
import com.aquaconnect.backend.repository.IvrSessionRepository;

@Service
public class IvrSessionService {

    private final IvrSessionRepository sessionRepository;
    private final IncidentService incidentService;

    public IvrSessionService(IvrSessionRepository sessionRepository, IncidentService incidentService) {
        this.sessionRepository = sessionRepository;
        this.incidentService = incidentService;
    }

    @Transactional
    public IvrSessionResponse start(String callerIdentifier) {
        return response(sessionRepository.save(new IvrSession(callerIdentifier)));
    }

    @Transactional
    public IvrSessionResponse selectLanguage(UUID sessionId, IvrLanguageRequest request) {
        IvrSession session = get(sessionId);
        requireState(session, IvrSessionState.START, IvrSessionState.LANGUAGE_SELECTION);
        session.selectLanguage(request.language());
        return response(sessionRepository.save(session));
    }

    @Transactional
    public IvrSessionResponse menu(UUID sessionId, int choice) {
        IvrSession session = get(sessionId);
        requireState(session, IvrSessionState.MAIN_MENU);
        switch (choice) {
            case 1 -> session.chooseReport();
            case 2 -> session.chooseCheckComplaint();
            case 3 -> session.chooseWaterSupplyInfo();
            case 4 -> session.chooseOperator();
            case 9 -> session.repeatMenu();
            default -> throw new IllegalArgumentException("Unsupported IVR menu choice");
        }
        return response(sessionRepository.save(session));
    }

    @Transactional
    public IvrSessionResponse details(UUID sessionId, IvrDetailsRequest request) {
        IvrSession session = get(sessionId);
        requireState(session, IvrSessionState.REPORT_PROBLEM, IvrSessionState.COLLECTING_DETAILS);
        session.details(request.category(), request.description().trim());
        return response(sessionRepository.save(session));
    }

    @Transactional
    public IvrSessionResponse location(UUID sessionId, IvrLocationRequest request) {
        IvrSession session = get(sessionId);
        requireState(session, IvrSessionState.CONFIRMATION);
        session.location(request.latitude(), request.longitude(), request.locationSource(), request.locationAccuracy());
        return response(sessionRepository.save(session));
    }

    @Transactional
    public IvrSessionResponse confirm(UUID sessionId, boolean confirmed) {
        IvrSession session = get(sessionId);
        requireState(session, IvrSessionState.CONFIRMATION);
        if (!confirmed) {
            session.fail();
            return response(sessionRepository.save(session));
        }
        IncidentResponse incident = incidentService.createForIvr(session.getCallerIdentifier(),
                new IncidentCreateRequest(session.getSelectedCategory(), session.getDescription(), session.getLatitude(),
                        session.getLongitude(), session.getLocationSource(), session.getLocationAccuracy(), IncidentSource.IVR));
        session.markIncidentCreated(incident.id());
        session.complete();
        return response(sessionRepository.save(session));
    }

    @Transactional(readOnly = true)
    public IvrSessionResponse getResponse(UUID sessionId) { return response(get(sessionId)); }

    @Transactional(readOnly = true)
    public List<IncidentResponse> complaints(UUID sessionId) {
        return incidentService.findForIvrCaller(get(sessionId).getCallerIdentifier());
    }

    private IvrSession get(UUID id) {
        return sessionRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("IVR session not found"));
    }

    private void requireState(IvrSession session, IvrSessionState... allowed) {
        for (IvrSessionState state : allowed) if (session.getState() == state) return;
        throw new IllegalArgumentException("Invalid IVR session state transition");
    }

    private IvrSessionResponse response(IvrSession session) { return IvrSessionResponse.from(session); }
}
