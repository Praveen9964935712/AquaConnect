package com.aquaconnect.backend.service;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.aquaconnect.backend.dto.incident.IncidentCreateRequest;
import com.aquaconnect.backend.dto.incident.IncidentResponse;
import com.aquaconnect.backend.dto.ivr.IvrComplaintStatusResponse;
import com.aquaconnect.backend.dto.ivr.IvrDetailsRequest;
import com.aquaconnect.backend.dto.ivr.IvrLanguageRequest;
import com.aquaconnect.backend.dto.ivr.IvrLocationRequest;
import com.aquaconnect.backend.dto.ivr.IvrSessionResponse;
import com.aquaconnect.backend.entity.IvrSession;
import com.aquaconnect.backend.enums.IncidentSource;
import com.aquaconnect.backend.enums.IncidentStatus;
import com.aquaconnect.backend.enums.IvrSessionState;
import com.aquaconnect.backend.enums.IvrSessionStatus;
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
        if (session.getIncidentId() != null) {
            throw new IllegalStateException("IVR session already has an incident");
        }
        if (!confirmed) {
            session.fail();
            return response(sessionRepository.save(session));
        }
        if (session.getSelectedCategory() == null || session.getDescription() == null || session.getDescription().isBlank()) {
            throw new IllegalArgumentException("Missing required IVR incident details");
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
    public IvrSessionResponse getResponseForCaller(UUID sessionId, String callerIdentifier) {
        IvrSession session = get(sessionId);
        requireCallerOwnership(session, callerIdentifier);
        return response(session);
    }

    @Transactional(readOnly = true)
    public List<IncidentResponse> complaints(UUID sessionId) {
        return incidentService.findForIvrCaller(get(sessionId).getCallerIdentifier());
    }

    @Transactional(readOnly = true)
    public List<IvrComplaintStatusResponse> complaintStatusesForCaller(UUID sessionId, String callerIdentifier) {
        IvrSession session = get(sessionId);
        requireCallerOwnership(session, callerIdentifier);
        if (session.getSessionStatus() != IvrSessionStatus.ACTIVE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "IVR session is no longer active");
        }
        List<IncidentResponse> incidents = incidentService.findForIvrCaller(session.getCallerIdentifier());
        if (incidents.isEmpty()) {
            return List.of(new IvrComplaintStatusResponse("NO_COMPLAINTS", "NONE", null, "NONE",
                    "No complaints were found for this caller."));
        }
        return java.util.stream.IntStream.range(0, incidents.size())
                .mapToObj(index -> mapCitizenComplaint(index, incidents.get(index)))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<IncidentResponse> complaintsForCaller(UUID sessionId, String callerIdentifier) {
        IvrSession session = get(sessionId);
        requireCallerOwnership(session, callerIdentifier);
        if (session.getSessionStatus() != IvrSessionStatus.ACTIVE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "IVR session is no longer active");
        }
        return incidentService.findForIvrCaller(session.getCallerIdentifier());
    }

    private IvrSession get(UUID id) {
        return sessionRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("IVR session not found"));
    }

    private void requireCallerOwnership(IvrSession session, String callerIdentifier) {
        if (callerIdentifier == null || callerIdentifier.isBlank()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "IVR caller not provided");
        }
        if (!callerIdentifier.equals(session.getCallerIdentifier())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "IVR caller does not own this session");
        }
    }

    private IvrComplaintStatusResponse mapCitizenComplaint(int index, IncidentResponse incident) {
        IncidentStatus status = incident.status();
        return new IvrComplaintStatusResponse(
                String.format("CMP-%04d", index + 1),
                incident.category() == null ? "UNKNOWN" : incident.category().name(),
                incident.reportedAt(),
                status == null ? "UNKNOWN" : status.name(),
                statusMessage(status));
    }

    private String statusMessage(IncidentStatus status) {
        if (status == null) {
            return "Your complaint is being reviewed.";
        }
        return switch (status) {
            case SUBMITTED -> "Your complaint has been received.";
            case UNDER_VERIFICATION -> "Your complaint is being verified.";
            case VERIFIED -> "Your complaint has been verified.";
            case ASSIGNED -> "Your complaint has been assigned to the field team.";
            case IN_PROGRESS -> "Work is currently in progress.";
            case REPAIR_COMPLETED -> "Repair work has been completed and is awaiting verification.";
            case AUTHORITY_VERIFICATION -> "Your complaint is awaiting authority verification.";
            case RESOLVED -> "Your complaint has been resolved.";
            case CLOSED -> "Your complaint has been closed.";
            case REOPENED -> "Your complaint has been reopened for further verification.";
        };
    }

    private void requireState(IvrSession session, IvrSessionState... allowed) {
        for (IvrSessionState state : allowed) if (session.getState() == state) return;
        throw new IllegalArgumentException("Invalid IVR session state transition");
    }

    private IvrSessionResponse response(IvrSession session) { return IvrSessionResponse.from(session); }
}
