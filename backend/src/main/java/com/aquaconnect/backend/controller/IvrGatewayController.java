package com.aquaconnect.backend.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.aquaconnect.backend.dto.incident.IncidentResponse;
import com.aquaconnect.backend.dto.ivr.IvrComplaintStatusResponse;
import com.aquaconnect.backend.dto.ivr.IvrSessionResponse;
import com.aquaconnect.backend.dto.ivr.StartIvrSessionRequest;
import com.aquaconnect.backend.entity.IvrEscalationRequest;
import com.aquaconnect.backend.enums.IvrLanguage;
import com.aquaconnect.backend.service.IvrEscalationService;
import com.aquaconnect.backend.service.IvrSessionService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/ivr")
public class IvrGatewayController {

    private final IvrSessionService sessionService;
    private final IvrEscalationService escalationService;
    private final String ivrTrustToken;

    public IvrGatewayController(IvrSessionService sessionService, IvrEscalationService escalationService,
            @Value("${app.ivr.trust-token:dev-local-ivr-token}") String ivrTrustToken) {
        this.sessionService = sessionService;
        this.escalationService = escalationService;
        this.ivrTrustToken = ivrTrustToken;
    }

    @PostMapping("/gateway/sessions")
    @ResponseStatus(HttpStatus.CREATED)
    public IvrSessionResponse start(
            @RequestHeader(value = "X-IVR-Trust", required = false) String trustToken,
            @RequestHeader(value = "X-IVR-Caller-Id", required = false) String callerIdentifier,
            @Valid @RequestBody StartIvrSessionRequest request) {
        validateTrust(trustToken, callerIdentifier);
        if (request.callerIdentifier() != null && !callerIdentifier.equals(request.callerIdentifier())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Caller identifier must match trusted caller");
        }
        return sessionService.start(callerIdentifier);
    }

    @GetMapping("/gateway/sessions/{id}")
    public IvrSessionResponse session(
            @RequestHeader(value = "X-IVR-Trust", required = false) String trustToken,
            @RequestHeader(value = "X-IVR-Caller-Id", required = false) String callerIdentifier,
            @PathVariable UUID id) {
        validateTrust(trustToken, callerIdentifier);
        return sessionService.getResponseForCaller(id, callerIdentifier);
    }

    @GetMapping("/gateway/sessions/{id}/complaints")
    public List<IvrComplaintStatusResponse> complaints(
            @RequestHeader(value = "X-IVR-Trust", required = false) String trustToken,
            @RequestHeader(value = "X-IVR-Caller-Id", required = false) String callerIdentifier,
            @PathVariable UUID id) {
        validateTrust(trustToken, callerIdentifier);
        return sessionService.complaintStatusesForCaller(id, callerIdentifier);
    }

    @PostMapping("/gateway/sessions/{id}/escalate")
    @ResponseStatus(HttpStatus.CREATED)
    public IvrEscalationRequest escalate(
            @RequestHeader(value = "X-IVR-Trust", required = false) String trustToken,
            @RequestHeader(value = "X-IVR-Caller-Id", required = false) String callerIdentifier,
            @PathVariable UUID id,
            @RequestParam IvrLanguage language,
            @RequestParam String reason) {
        validateTrust(trustToken, callerIdentifier);
        return escalationService.createForSession(id, callerIdentifier, language, reason);
    }

    private void validateTrust(String trustToken, String callerIdentifier) {
        if (trustToken == null || trustToken.isBlank() || !ivrTrustToken.equals(trustToken)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "IVR trust validation failed");
        }
        if (callerIdentifier == null || callerIdentifier.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "IVR caller identifier is required");
        }
    }
}
