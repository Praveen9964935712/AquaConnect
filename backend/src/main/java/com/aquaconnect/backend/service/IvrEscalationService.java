package com.aquaconnect.backend.service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.aquaconnect.backend.entity.IvrSession;

import com.aquaconnect.backend.entity.IvrEscalationRequest;
import com.aquaconnect.backend.enums.IvrEscalationStatus;
import com.aquaconnect.backend.enums.IvrLanguage;
import com.aquaconnect.backend.repository.IvrEscalationRequestRepository;
import com.aquaconnect.backend.repository.IvrSessionRepository;

@Service
public class IvrEscalationService {

    private final IvrEscalationRequestRepository escalationRepository;
    private final IvrSessionRepository sessionRepository;

    public IvrEscalationService(IvrEscalationRequestRepository escalationRepository, IvrSessionRepository sessionRepository) {
        this.escalationRepository = escalationRepository;
        this.sessionRepository = sessionRepository;
    }

    @Transactional
    public IvrEscalationRequest create(UUID sessionId, String callerIdentifier, IvrLanguage language, String reason) {
        if (reason == null || reason.isBlank() || reason.length() > 1000) {
            throw new IllegalArgumentException("IVR escalation reason is invalid");
        }
        var session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("IVR session not found"));
        requireActiveSession(session);
        if (callerIdentifier == null || callerIdentifier.isBlank()) {
            throw new IllegalArgumentException("IVR caller identifier is required");
        }
        if (!callerIdentifier.equals(session.getCallerIdentifier())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "IVR caller does not own this session");
        }
        if (escalationRepository.existsBySessionId(sessionId)) {
            throw new IllegalStateException("Escalation already queued for this session");
        }
        IvrEscalationRequest request = new IvrEscalationRequest(sessionId, callerIdentifier, language, reason);
        request.setStatus(IvrEscalationStatus.QUEUED);
        return escalationRepository.save(request);
    }

    @Transactional
    public IvrEscalationRequest createForSession(UUID sessionId, String callerIdentifier, IvrLanguage language, String reason) {
        return create(sessionId, callerIdentifier, language, reason);
    }

    @Transactional(readOnly = true)
    public List<IvrEscalationRequest> findPending() {
        return escalationRepository.findByStatusOrderByCreatedAtAsc(IvrEscalationStatus.QUEUED);
    }

    @Transactional(readOnly = true)
    public IvrEscalationRequest findById(UUID id) {
        return escalationRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Escalation not found"));
    }

    @Transactional
    public IvrEscalationRequest accept(UUID id, UUID operatorId) {
        IvrEscalationRequest request = findById(id);
        if (operatorId == null) {
            throw new IllegalArgumentException("Operator identifier is required");
        }
        if (request.getStatus() == IvrEscalationStatus.QUEUED) {
            request.setStatus(IvrEscalationStatus.ACCEPTED);
            request.setAssignedOperatorId(operatorId);
            return escalationRepository.save(request);
        }
        if (request.getStatus() == IvrEscalationStatus.COMPLETED || request.getStatus() == IvrEscalationStatus.CANCELLED) {
            throw new IllegalStateException("Invalid escalation state transition");
        }
        throw new IllegalStateException("Invalid escalation state transition");
    }

    @Transactional
    public IvrEscalationRequest markInProgress(UUID id) {
        IvrEscalationRequest request = findById(id);
        if (request.getStatus() != IvrEscalationStatus.ACCEPTED) {
            throw new IllegalStateException("Invalid escalation state transition");
        }
        request.setStatus(IvrEscalationStatus.IN_PROGRESS);
        return escalationRepository.save(request);
    }

    @Transactional
    public IvrEscalationRequest complete(UUID id) {
        IvrEscalationRequest request = findById(id);
        if (request.getStatus() != IvrEscalationStatus.IN_PROGRESS) {
            throw new IllegalStateException("Invalid escalation state transition");
        }
        request.setStatus(IvrEscalationStatus.COMPLETED);
        request.setCompletedAt(Instant.now());
        return escalationRepository.save(request);
    }

    @Transactional
    public IvrEscalationRequest cancel(UUID id) {
        IvrEscalationRequest request = findById(id);
        if (request.getStatus() == IvrEscalationStatus.COMPLETED || request.getStatus() == IvrEscalationStatus.CANCELLED) {
            throw new IllegalStateException("Invalid escalation state transition");
        }
        request.setStatus(IvrEscalationStatus.CANCELLED);
        request.setCompletedAt(Instant.now());
        return escalationRepository.save(request);
    }

    private void requireActiveSession(IvrSession session) {
        if (session.getSessionStatus() != com.aquaconnect.backend.enums.IvrSessionStatus.ACTIVE) {
            throw new IllegalStateException("IVR session is no longer active");
        }
    }
}
