package com.aquaconnect.backend.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.aquaconnect.backend.entity.IvrEscalationRequest;
import com.aquaconnect.backend.enums.IvrLanguage;
import com.aquaconnect.backend.service.IvrEscalationService;

@RestController
@RequestMapping("/api/ivr")
@PreAuthorize("hasAnyRole('OPERATOR', 'OPERATIONS_MANAGER', 'ADMIN')")
public class IvrEscalationController {

    private final IvrEscalationService escalationService;

    public IvrEscalationController(IvrEscalationService escalationService) {
        this.escalationService = escalationService;
    }

    @PostMapping("/sessions/{sessionId}/escalate")
    @ResponseStatus(HttpStatus.CREATED)
    public IvrEscalationRequest create(
            @PathVariable UUID sessionId,
            @RequestParam String callerIdentifier,
            @RequestParam IvrLanguage language,
            @RequestParam String reason) {
        return escalationService.create(sessionId, callerIdentifier, language, reason);
    }

    @GetMapping("/escalations/pending")
    public List<IvrEscalationRequest> pending() {
        return escalationService.findPending();
    }

    @PostMapping("/escalations/{id}/accept")
    public IvrEscalationRequest accept(@PathVariable UUID id, @RequestParam UUID operatorId) {
        return escalationService.accept(id, operatorId);
    }

    @PostMapping("/escalations/{id}/complete")
    public IvrEscalationRequest complete(@PathVariable UUID id) {
        return escalationService.complete(id);
    }

    @PostMapping("/escalations/{id}/cancel")
    public IvrEscalationRequest cancel(@PathVariable UUID id) {
        return escalationService.cancel(id);
    }
}
