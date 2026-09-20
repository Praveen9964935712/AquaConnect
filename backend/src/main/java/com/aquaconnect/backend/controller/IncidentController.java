package com.aquaconnect.backend.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aquaconnect.backend.dto.incident.IncidentCreateRequest;
import com.aquaconnect.backend.dto.incident.IncidentResponse;
import com.aquaconnect.backend.service.IncidentService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/incidents")
@PreAuthorize("hasRole('CITIZEN')")
public class IncidentController {

    private final IncidentService incidentService;

    public IncidentController(IncidentService incidentService) {
        this.incidentService = incidentService;
    }

    @PostMapping
    public ResponseEntity<IncidentResponse> create(
            @Valid @RequestBody IncidentCreateRequest request,
            Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(incidentService.createForCitizen(userId(authentication), request));
    }

    @GetMapping
    public List<IncidentResponse> findAll(Authentication authentication) {
        return incidentService.findAllForCitizen(userId(authentication));
    }

    @GetMapping("/{id}")
    public IncidentResponse findById(@PathVariable UUID id, Authentication authentication) {
        return incidentService.findForCitizen(userId(authentication), id);
    }

    private UUID userId(Authentication authentication) {
        return UUID.fromString(authentication.getName());
    }
}
