package com.aquaconnect.backend.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.aquaconnect.backend.dto.evidence.EvidenceResponse;
import com.aquaconnect.backend.enums.EvidenceType;
import com.aquaconnect.backend.service.EvidenceService;

@RestController
@RequestMapping("/api/work-orders")
@PreAuthorize("hasAnyRole('OPERATOR', 'OPERATIONS_MANAGER', 'FIELD_ENGINEER', 'ADMIN')")
public class EvidenceController {

    private final EvidenceService evidenceService;

    public EvidenceController(EvidenceService evidenceService) { this.evidenceService = evidenceService; }

    @PostMapping(value = "/{id}/evidence", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('FIELD_ENGINEER')")
    public EvidenceResponse upload(@PathVariable UUID id, @RequestPart("type") EvidenceType type,
            @RequestPart("file") MultipartFile file, Authentication authentication) {
        return evidenceService.upload(id, type, file, authentication);
    }

    @GetMapping("/{id}/evidence")
    public List<EvidenceResponse> list(@PathVariable UUID id, Authentication authentication) {
        return evidenceService.list(id, authentication);
    }

}
