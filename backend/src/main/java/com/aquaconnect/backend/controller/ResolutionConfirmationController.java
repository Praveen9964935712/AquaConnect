package com.aquaconnect.backend.controller;

import java.util.UUID;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aquaconnect.backend.dto.confirmation.ResolutionConfirmationRequest;
import com.aquaconnect.backend.dto.confirmation.ResolutionConfirmationResponse;
import com.aquaconnect.backend.service.ResolutionConfirmationService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/incidents")
@PreAuthorize("hasRole('CITIZEN')")
public class ResolutionConfirmationController {

    private final ResolutionConfirmationService confirmationService;

    public ResolutionConfirmationController(ResolutionConfirmationService confirmationService) {
        this.confirmationService = confirmationService;
    }

    @PostMapping("/{id}/resolution-confirmation")
    public ResolutionConfirmationResponse confirm(@PathVariable UUID id,
            @Valid @RequestBody ResolutionConfirmationRequest request, Authentication authentication) {
        return confirmationService.confirm(id, request, authentication);
    }
}
