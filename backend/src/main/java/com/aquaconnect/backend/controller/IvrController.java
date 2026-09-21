package com.aquaconnect.backend.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.aquaconnect.backend.dto.incident.IncidentResponse;
import com.aquaconnect.backend.dto.ivr.IvrDetailsRequest;
import com.aquaconnect.backend.dto.ivr.IvrLanguageRequest;
import com.aquaconnect.backend.dto.ivr.IvrLocationRequest;
import com.aquaconnect.backend.dto.ivr.IvrMenuChoiceRequest;
import com.aquaconnect.backend.dto.ivr.IvrSessionResponse;
import com.aquaconnect.backend.dto.ivr.StartIvrSessionRequest;
import com.aquaconnect.backend.service.IvrSessionService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/ivr")
@PreAuthorize("hasAnyRole('OPERATOR', 'OPERATIONS_MANAGER', 'ADMIN')")
public class IvrController {

    private final IvrSessionService sessionService;

    public IvrController(IvrSessionService sessionService) { this.sessionService = sessionService; }

    @PostMapping("/sessions")
    @ResponseStatus(HttpStatus.CREATED)
    public IvrSessionResponse start(@Valid @RequestBody StartIvrSessionRequest request) {
        return sessionService.start(request.callerIdentifier());
    }

    @PostMapping("/sessions/{id}/language")
    public IvrSessionResponse language(@PathVariable UUID id, @Valid @RequestBody IvrLanguageRequest request) {
        return sessionService.selectLanguage(id, request);
    }

    @PostMapping("/sessions/{id}/menu")
    public IvrSessionResponse menu(@PathVariable UUID id, @Valid @RequestBody IvrMenuChoiceRequest request) {
        return sessionService.menu(id, request.choice());
    }

    @PostMapping("/sessions/{id}/details")
    public IvrSessionResponse details(@PathVariable UUID id, @Valid @RequestBody IvrDetailsRequest request) {
        return sessionService.details(id, request);
    }

    @PostMapping("/sessions/{id}/location")
    public IvrSessionResponse location(@PathVariable UUID id, @Valid @RequestBody IvrLocationRequest request) {
        return sessionService.location(id, request);
    }

    @PostMapping("/sessions/{id}/confirmation")
    public IvrSessionResponse confirmation(@PathVariable UUID id, @RequestParam boolean confirmed) {
        return sessionService.confirm(id, confirmed);
    }

    @GetMapping("/sessions/{id}")
    public IvrSessionResponse session(@PathVariable UUID id) { return sessionService.getResponse(id); }

    @GetMapping("/sessions/{id}/complaints")
    public List<IncidentResponse> complaints(@PathVariable UUID id) { return sessionService.complaints(id); }
}
