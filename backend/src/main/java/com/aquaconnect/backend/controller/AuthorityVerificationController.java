package com.aquaconnect.backend.controller;

import java.util.UUID;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aquaconnect.backend.dto.verification.AuthorityVerificationRequest;
import com.aquaconnect.backend.dto.verification.AuthorityVerificationResponse;
import com.aquaconnect.backend.service.AuthorityVerificationService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/work-orders")
@PreAuthorize("hasAnyRole('OPERATOR', 'OPERATIONS_MANAGER', 'ADMIN')")
public class AuthorityVerificationController {

    private final AuthorityVerificationService verificationService;

    public AuthorityVerificationController(AuthorityVerificationService verificationService) {
        this.verificationService = verificationService;
    }

    @PostMapping("/{id}/verify")
    public AuthorityVerificationResponse verify(@PathVariable UUID id,
            @Valid @RequestBody AuthorityVerificationRequest request, Authentication authentication) {
        return verificationService.verify(id, request, authentication);
    }
}
